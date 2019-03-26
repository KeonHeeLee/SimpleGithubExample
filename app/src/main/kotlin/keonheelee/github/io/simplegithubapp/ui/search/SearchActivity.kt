package keonheelee.github.io.simplegithubapp.ui.search

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import com.jakewharton.rxbinding2.widget.queryTextChangeEvents
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import keonheelee.github.io.simplegithubapp.rx.AutoClearedDisposable

import keonheelee.github.io.simplegithubapp.R
import keonheelee.github.io.simplegithubapp.api.GithubApi
import keonheelee.github.io.simplegithubapp.api.Model.GithubRepo
import keonheelee.github.io.simplegithubapp.api.provideGithubApi
import keonheelee.github.io.simplegithubapp.data.provideSearchHistoryDao
import keonheelee.github.io.simplegithubapp.extensions.plusAssign
import keonheelee.github.io.simplegithubapp.extensions.runOnIoScheduler
import keonheelee.github.io.simplegithubapp.ui.repo.RepositoryActivity

import kotlinx.android.synthetic.main.activity_search.*
import java.lang.IllegalStateException
import org.jetbrains.anko.startActivity

class SearchActivity : AppCompatActivity(), SearchAdapter.ItemClickListener {

    lateinit internal var menuSearch: MenuItem
    lateinit internal var searchView: SearchView

    internal val adapter: SearchAdapter by lazy {
        SearchAdapter().apply { setItemClickListener(this@SearchActivity) } }

    internal val api: GithubApi by lazy { provideGithubApi(this) }
    // 여러 디스포저블 객체를 관리할 수 있는 CompositeDiposable 객체를 초기화
    // internal var searchCall: Call<RepoSearchResponse>? = null
    internal val disposables = AutoClearedDisposable(this)

    internal val viewDisposables = AutoClearedDisposable(
            lifecycleOwner = this, alwaysClearOnStop = false)

    // SearchHistoryDao의 인스턴스를 받아옴
    internal val searchHistoryDao by lazy { provideSearchHistoryDao(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        // Lifecycle.addObserver() 함수를 사용하여 각 객체를 옵서버로 등록
        lifecycle += disposables
        lifecycle += viewDisposables

        // 검색 결과를 표시할 어댑터를 리사이클러뷰에 설정
        adapter.setItemClickListener(this)
        with(rvActivitySearchList){
            layoutManager = LinearLayoutManager(this@SearchActivity)
            adapter = adapter
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
        disposables += runOnIoScheduler { searchHistoryDao.add(repository) }

        // 검색 결과를 선택하면 자세한 정보를 표시하는 액티비티 실행
        startActivity<RepositoryActivity>(
                RepositoryActivity.KEY_USER_LOGIN to repository.owner.login,
                RepositoryActivity.KEY_REPO_NAME to repository.name)
    }

    private fun searchRepository(query: String) {
        // REST API를 통해 검색 결과를 요청

        disposables += api.searchRepository(query)
                .flatMap {
                    if(it.totalCount == 0){
                        // 검색결과가 없을 경우
                        // 에러를 발생시켜 에러 메세지를 표시하도록 함
                        // (곧바로 에러 블록이 실행)
                        Observable.error(IllegalStateException("No Search result"))
                    } else {
                        Observable.just(it.items)
                    }
                }
                // 이 이후에 수행되는 코드는 모두 메인 스레드에서 실행
                // RxAndroid에서 제공하는 스케줄러인
                // AndroidScheduler.mainThread()를 사용
                .observeOn(AndroidSchedulers.mainThread())

                // 구독할 때 수행할 작업을 구현
                .doOnSubscribe {
                    clearResults()
                    hideError()
                    showProgress()
                }

                // 스트림이 종료될 때 수행할 작업을 구현
                .doOnTerminate { hideProgress() }

                // 옵서버블을 구독
                .subscribe({ items ->

                    // API를 통해 검색 결과를 정상적으로 받았을 때 처리할 작업을 구현
                    // 작업중 오류가 발생하면 이 블록은 호출되지 않음
                    with(adapter){
                        setItems(items)
                        notifyDataSetChanged()
                    }
                }){
                    // 에러 블록
                    // 네트워크 오류나 데이터 처리 오류 등
                    // 작업이 정상적으로 완료되지 않았을 때 호출
                    showError(it.message)
                }
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

    private fun clearResults() {
        with(adapter) {
            clearItems()
            notifyDataSetChanged()
        }
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
