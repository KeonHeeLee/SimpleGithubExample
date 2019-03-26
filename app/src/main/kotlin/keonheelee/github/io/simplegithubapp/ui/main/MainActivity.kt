package keonheelee.github.io.simplegithubapp.ui.main

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

import keonheelee.github.io.simplegithubapp.R
import keonheelee.github.io.simplegithubapp.api.Model.GithubRepo
import keonheelee.github.io.simplegithubapp.data.provideSearchHistoryDao
import keonheelee.github.io.simplegithubapp.extensions.plusAssign
import keonheelee.github.io.simplegithubapp.extensions.runOnIoScheduler
import keonheelee.github.io.simplegithubapp.rx.AutoClearedDisposable
import keonheelee.github.io.simplegithubapp.ui.repo.RepositoryActivity
import keonheelee.github.io.simplegithubapp.ui.search.SearchActivity
import keonheelee.github.io.simplegithubapp.ui.search.SearchAdapter

import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.startActivity

class MainActivity : AppCompatActivity(), SearchAdapter.ItemClickListener {

    // 어댑터 프로퍼티를 추가
    internal val adapter by lazy {
        SearchAdapter().apply { setItemClickListener(this@MainActivity) }
    }

    // 최근 조회한 저장소를 담당하는 데이터 접근 객체 프로퍼티를 추가
    internal val searchHistoryDao by lazy { provideSearchHistoryDao(this) }

    // 디스포저블을 관리하는 프로퍼티를 추가
    internal val disposables = AutoClearedDisposable(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lifecycle += disposables
        lifecycle += object: LifecycleObserver{
            // onStart() 콜백 함수가 호출되면 fetchSearchHistory() 함수를 호출
            @OnLifecycleEvent(Lifecycle.Event.ON_START)
            fun fetch(){
                fetchSearchHistory()
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
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_activity_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // 'Clear all' 메뉴를 선택하면 조회했던 저장소 기록을 모두 삭제
        if(R.id.menu_activity_main_clear_all == item.itemId){
            clearAll()
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

    // 데이터베이스에 저장되어 있는 저장소 목록을 불러오는 작업을 반환
    // SearchHistoryDao.getHistory() 함수는 Flowable 형태로 데이터를 반환하므로,
    // 데이터베이스에 저장된 자료가 바뀌면 즉시 업데이트된 정보가 새로 전달
    private fun fetchSearchHistory(): Disposable
        = searchHistoryDao.getHistory()
            // 메인 스레드에서 호출하면 Room에서 오류를 발생시키므로 IO 스레드에서 작업을 수행
            .subscribeOn(Schedulers.io())
            // 결과를 받아 뷰에 업데이트해야 하므로 메인 스레드(UI 스레드)에서 결과를 처리
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({items->

                // 어댑터를 갱신
                with(adapter){
                    setItems(items)
                    notifyDataSetChanged()
                }

                // 저장된 데이터의 유무에 따라 오류 메세지를 표시하거나 감춤
                if(items.isEmpty())
                    showMessage(getString(R.string.no_recent_repositories))
                else
                    hideMessage()
            }) {
                showMessage(it.message)
            }

    // 데이터베이스에 저장되어 있는 모든 저장소 기록을 삭제
    private fun clearAll(){
        // 메인 스레드에서 실행하면 오류가 발생하므로,
        // 앞에서 작성한 runIoScheduler() 함수를 사용하여 IO 스레드에서 작업을 실행
        disposables += runOnIoScheduler { searchHistoryDao.clearAll() }
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
