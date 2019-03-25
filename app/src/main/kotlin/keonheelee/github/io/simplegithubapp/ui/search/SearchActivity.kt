package keonheelee.github.io.simplegithubapp.ui.search

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView

import keonheelee.github.io.simplegithubapp.R
import keonheelee.github.io.simplegithubapp.ui.api.GithubApi
import keonheelee.github.io.simplegithubapp.ui.api.Model.GithubRepo
import keonheelee.github.io.simplegithubapp.ui.api.Model.RepoSearchResponse
import keonheelee.github.io.simplegithubapp.ui.api.Model.provideGithubApi
import keonheelee.github.io.simplegithubapp.ui.repo.RepositoryActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

import kotlinx.android.synthetic.main.activity_search.*
import org.jetbrains.anko.startActivity

class SearchActivity : AppCompatActivity(), SearchAdapter.ItemClickListener {

    lateinit internal var menuSearch: MenuItem
    lateinit internal var searchView: SearchView

    internal val adapter: SearchAdapter by lazy {
        SearchAdapter().apply { setItemClickListener(this@SearchActivity) } }

    internal val api: GithubApi by lazy { provideGithubApi(this) }
    internal var searchCall: Call<RepoSearchResponse>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

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
        searchView = (menuSearch.actionView as SearchView).apply{
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                // SearchView.onQueryTextListener 인터페이스를 구현하는
                // 익명 클래스의 인스턴스를 생성
                override fun onQueryTextSubmit(query: String): Boolean {
                    updateTitle(query)
                    hideSoftKeyboard()
                    collapseSearchView()
                    searchRepository(query)
                    return true
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    return false
                }
            })
        }

        // menuSearch 내 액션뷰인 SearchView를 펼침
        with(menuSearch){
            setOnActionExpandListener(object: MenuItem.OnActionExpandListener{
                override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                    return true
                }

                override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                    if(searchView.query == "")
                        finish()
                    return true
                }

            })
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
        // 검색 결과를 선택하면 자세한 정보를 표시하는 액티비티 실행
        startActivity<RepositoryActivity>(
                RepositoryActivity.KEY_USER_LOGIN to repository.owner.login,
                RepositoryActivity.KEY_REPO_NAME to repository.name
        )
    }

    private fun searchRepository(query: String) {
        clearResults()
        hideError()
        showProgress()

        searchCall = api.searchRepository(query)

        // Call 인터페이스를 구현하는 익명 클래스의 인스턴스를 생성
        searchCall!!.enqueue(object : Callback<RepoSearchResponse> {
            override fun onResponse(call: Call<RepoSearchResponse>,
                                    response: Response<RepoSearchResponse>) {
                hideProgress()

                val searchResult = response.body()
                if (response.isSuccessful && searchResult != null) {
                    // 검색 결과를 어댑터에 반영하고 갱신
                    with(adapter){
                        setItems(searchResult.items)
                        notifyDataSetChanged()
                    }

                    // 검색 결과가 없을 경우 에러 메세지를 표시
                    if (searchResult.totalCount == 0)
                        showError(getString(R.string.no_search_result))
                } else
                    showError("Not successful:" + response.message())
            }

            override fun onFailure(call: Call<RepoSearchResponse>, t: Throwable) {
                hideProgress()
                showError(t.message)
            }
        })
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

    override fun onStop(){
        super.onStop()
        searchCall?.run { cancel() }
    }
}
