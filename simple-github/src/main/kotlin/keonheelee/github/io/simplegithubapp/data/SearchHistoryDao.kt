package keonheelee.github.io.simplegithubapp.data

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import io.reactivex.Flowable
import keonheelee.github.io.simplegithubapp.api.Model.GithubRepo

@Dao
interface SearchHistoryDao {
    // 데이터베이스에 저장소를 추가
    // 이미 저장된 항목이 있을 경우 데이터를 덮어씀
    @Insert(onConflict =  OnConflictStrategy.REPLACE)
    fun add(repo: GithubRepo)

    // 저장되어 있는 저장소 목록을 반환
    // Flowable 형태의 자료를 반환하므로, 데이터베이스가 변경되면 알림을 받아 새로운 자료를 가져옴
    // 따라서 항상 최신 자료를 유지
    @Query("SELECT * FROM repositories")
    fun getHistory(): Flowable<List<GithubRepo>>

    // repositories 테이블의 모든 데이터를 삭제
    @Query("DELETE FROM repositories")
    fun clearAll()
}