package keonheelee.github.io.simplegithubapp.ui.main

import android.arch.lifecycle.*
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

import keonheelee.github.io.simplegithubapp.R
import keonheelee.github.io.simplegithubapp.api.Model.GithubRepo
import keonheelee.github.io.simplegithubapp.data.SearchHistoryDao
import keonheelee.github.io.simplegithubapp.data.provideSearchHistoryDao
import keonheelee.github.io.simplegithubapp.extensions.plusAssign
import keonheelee.github.io.simplegithubapp.rx.AutoActivatedDisposable
import keonheelee.github.io.simplegithubapp.rx.AutoClearedDisposable
import keonheelee.github.io.simplegithubapp.ui.repo.RepositoryActivity
import keonheelee.github.io.simplegithubapp.ui.search.SearchActivity
import keonheelee.github.io.simplegithubapp.ui.search.SearchAdapter

import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.startActivity
import javax.inject.Inject

class MainActivity : DaggerAppCompatActivity(), SearchAdapter.ItemClickListener {

    // 어댑터 프로퍼티를 추가
    internal val adapter by lazy {
        SearchAdapter().apply { setItemClickListener(this@MainActivity) }
    }

    // 최근 조회한 저장소를 담당하는 데이터 접근 객체 프로퍼티를 추가
    @Inject lateinit var searchHistoryDao: SearchHistoryDao

    // 디스포저블을 관리하는 프로퍼티를 추가
    internal val disposables = AutoClearedDisposable(this)

    // 액티비티가 완전히 종료되기 전까지 이벤트를 계속 받기 위해 추가
    internal val viewDisposables
            = AutoClearedDisposable(lifecycleOwner = this, alwaysClearOnStop = false)

    // MainViewModel을 생성하기 위해 필요한 뷰모델 팩트뢰 클래스의 인스턴스를 생성
    internal val viewModelFactory
            by lazy { MainViewModelFactory(searchHistoryDao) }

    // 뷰모델의 인스턴스는 onCreate()에서 받으므로, lateinit으로 선언
    lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // MainViewModel의 인스턴스를 받음
        viewModel = ViewModelProviders.of(
                this, viewModelFactory)[MainViewModel::class.java]

        lifecycle += disposables

        // viewDisposable에서 이 액티비티의 생명주기 이벤트를 받도록 함
        lifecycle += viewDisposables

        lifecycle += AutoActivatedDisposable(this) {
            viewModel.searchHistory
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe{items ->
                        with(adapter){
                            if(items.isEmpty)
                                clearItems()
                            else
                                setItems(items.value)
                            notifyDataSetChanged()
                        }
                    }
        }

        btnActivityMainSearch.setOnClickListener {
            // 저장소 검색 액티비티 호출
            startActivity<SearchActivity>()
        }

        // 리사이클러뷰에 어댑터를 설정
        with(rvActivityMainList){
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }

        // 메세지 이벤트를 구독
        viewDisposables += viewModel.message
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { message ->
                    if(message.isEmpty)
                        // 빈 메세지를 받은 경우 표시되고 있는 메세지를 화면에서 숨김
                        hideMessage()
                    else
                        // 유효한 메세지를 받은 경우 화면에 메세지를 표시
                        showMessage(message.value)
                }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_activity_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // 'Clear all' 메뉴를 선택하면 조회했던 저장소 기록을 모두 삭제
        if(R.id.menu_activity_main_clear_all == item.itemId){
            disposables += viewModel.clearSearchHistory()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onItemClick(repository: GithubRepo) {
        startActivity<RepositoryActivity>(
                RepositoryActivity.KEY_USER_LOGIN to repository.owner.login,
                RepositoryActivity.KEY_REPO_NAME to repository.name
        )
    }

    private fun showMessage(message: String?){
        with(tvActivityMainMessage){
            text = message ?: "Unexpected error."
            visibility = View.VISIBLE
        }
    }

    private fun hideMessage(){
        with(tvActivityMainMessage){
            text = ""
            visibility = View.GONE
        }
    }
}
