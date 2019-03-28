package keonheelee.github.io.simplegithubapp.ui.search

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import com.jakewharton.rxbinding2.widget.queryTextChangeEvents
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import keonheelee.github.io.simplegithubapp.rx.AutoClearedDisposable

import keonheelee.github.io.simplegithubapp.R
import keonheelee.github.io.simplegithubapp.api.GithubApi
import keonheelee.github.io.simplegithubapp.api.Model.GithubRepo
import keonheelee.github.io.simplegithubapp.api.provideGithubApi
import keonheelee.github.io.simplegithubapp.data.SearchHistoryDao
import keonheelee.github.io.simplegithubapp.extensions.plusAssign
import keonheelee.github.io.simplegithubapp.ui.repo.RepositoryActivity

import kotlinx.android.synthetic.main.activity_search.*
import org.jetbrains.anko.startActivity
import javax.inject.Inject

class SearchActivity : DaggerAppCompatActivity(), SearchAdapter.ItemClickListener {

    lateinit internal var menuSearch: MenuItem
    lateinit internal var searchView: SearchView

    @Inject lateinit var adapter: SearchAdapter

    internal val api: GithubApi by lazy { provideGithubApi(this) }
    // 여러 디스포저블 객체를 관리할 수 있는 CompositeDiposable 객체를 초기화
    // internal var searchCall: Call<RepoSearchResponse>? = null
    internal val disposables = AutoClearedDisposable(this)

    internal val viewDisposables = AutoClearedDisposable(
            lifecycleOwner = this, alwaysClearOnStop = false)

    // SearchHistoryDao의 인스턴스를 받아옴
    @Inject lateinit var searchHistoryDao: SearchHistoryDao

    @Inject lateinit var githubApi: GithubApi

    // SearchViewModel을 생성할 때 필요한 뷰모델 팩토리 클래스의 인스턴스를 생성
    @Inject lateinit var viewModelFactory: SearchViewModelFactory

    lateinit var viewModel: SearchViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        // SearchViewModel의 인스턴스를 받음
        viewModel = ViewModelProviders.of(
                this, viewModelFactory)[SearchViewModel::class.java]

        // Lifecycle.addObserver() 함수를 사용하여 각 객체를 옵서버로 등록
        lifecycle += disposables
        lifecycle += viewDisposables

        // 검색 결과를 표시할 어댑터를 리사이클러뷰에 설정
        adapter.setItemClickListener(this)
        with(rvActivitySearchList){
            layoutManager = LinearLayoutManager(this@SearchActivity)
            adapter = adapter
        }

        // 검색 결과 이벤트를 구독
        viewDisposables += viewModel.searchResult
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { items ->
                    with(adapter) {
                        if(items.isEmpty)
                            clearItems()
                        else
                            setItems(items.value)

                        notifyDataSetChanged()
                    }
                }

        // 메세지 이벤트를 구독
        viewDisposables += viewModel.message
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe{ message ->
                    if(message.isEmpty)
                        hideError()
                    else
                        showError(message.value)
                }

        // 작업 진행 여부 이벤트 구독
        viewDisposables += viewModel.isLoading
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { isLoaing ->
                    if(isLoaing)
                        showProgress()
                    else
                        hideProgress()
                }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_activity_search, menu)

        menuSearch = menu.findItem(R.id.menu_activity_search_query)
        // 검색어를 처리할 SearchView를 설정
        // menueSearch.actionView를 SearchView로 캐스팅
        searchView = (menuSearch.actionView as SearchView)

        // SearchView에서 발생하는 이벤트를 옵서버블 형태로 받음
        viewDisposables += searchView.queryTextChangeEvents()
                // 검색을 수행했을 때 발생한 이벤트만 받음
                .filter { it.isSubmitted }
                // 이벤트에서 검색어 텍스트(CharSequence)를 추출
                .map { it.queryText() }
                // 빈 문자열이 아닌 검색어만 받음
                .filter { it.isNotEmpty() }
                // 검색어를 String 형태로 변환
                .map { it.toString() }
                // 이 이후에 수행되는 코드는 모두 메인 스레드에서 실행
                // RxAndroid에서 제공하는 스케줄러인 AndroidScheduler.mainThread()를 사용
                .observeOn(AndroidSchedulers.mainThread())
                // 옵서버블을 구독
                .subscribe {query->
                    updateTitle(query)
                    hideSoftKeyboard()
                    collapseSearchView()
                    searchRepository(query)

        }
        // menuSearch 내 액션뷰인 SearchView를 펼침
        menuSearch.expandActionView()

        // 마지막으로 검색한 검색어 이벤트를 구독
        viewDisposables += viewModel.lastSearchKeyword
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { keyword ->
                    if(keyword.isEmpty)
                        menuSearch.expandActionView()
                    else
                        updateTitle(keyword.value)
                }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (R.id.menu_activity_search_query == item.itemId) {
            item.expandActionView()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onItemClick(repository: GithubRepo) {
        // 데이터베이스에 저장소를 추가
        // 데이터 조작 코드를 메인 스레드에서 호출하면 에러가 발생하므로,
        // RxJava의 Completable을 사용하여
        // IO 스레드에서 데이터 추가 작업을 수행하도록 함
        disposables += viewModel.addToSearchHistory(repository)

        // 검색 결과를 선택하면 자세한 정보를 표시하는 액티비티 실행
        startActivity<RepositoryActivity>(
                RepositoryActivity.KEY_USER_LOGIN to repository.owner.login,
                RepositoryActivity.KEY_REPO_NAME to repository.name)
    }

    private fun searchRepository(query: String) {
        // REST API를 통해 검색 결과를 요청
        // 전달받은 검색어로 검색 결과를 요청
        disposables += viewModel.searchRepository(query)
    }

    private fun updateTitle(query: String) {
        supportActionBar?.run{ subtitle = query }
    }

    private fun hideSoftKeyboard() {
        (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).run {
            hideSoftInputFromWindow(searchView.windowToken, 0)
        }
    }

    private fun collapseSearchView() {
        menuSearch.collapseActionView()
    }

    private fun showProgress() {
        pbActivitySearch.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        pbActivitySearch.visibility = View.GONE
    }

    private fun showError(message: String?) {
        with(tvActivitySearchMessage) {
            text = message ?: "Unexpected error."
            visibility = View.VISIBLE
        }
    }

    private fun hideError() {
        with(tvActivitySearchMessage) {
            text = ""
            visibility = View.GONE
        }
    }
}
