package com.example.moneyfypro.data

import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.example.moneyfypro.data.QueryGenerator.Companion.queryStart
import com.example.moneyfypro.utils.ExpensesFilter
import java.util.*
import kotlin.collections.ArrayList

/**
 * Query generator for custom query.
 */
abstract class QueryGenerator {
    abstract fun queryBuilderWrapper(): QueryBuilderWrapper

    fun query(): SupportSQLiteQuery {
        val queryBuilderWrapper = queryBuilderWrapper()
        val query = queryBuilderWrapper.query
        val allQueries = queryStart + if (query.isNotBlank()) (" $filterKeyWord$query") else Unit
        println("all query is $allQueries")
        return SimpleSQLiteQuery(allQueries, queryBuilderWrapper.arguments.toArray())
    }

    data class QueryBuilderWrapper(
        var query: String,
        val arguments: ArrayList<Any>
    )

    companion object {
        const val queryStart = "SELECT * FROM expense"
        const val filterKeyWord = "WHERE"

    }
}

/**
 * Query generator for combined filter
 */
class CombinedFilterQueryGenerator(private val queryGenerators: List<QueryGenerator>) : QueryGenerator() {
    override fun queryBuilderWrapper(): QueryBuilderWrapper {
        println(queryGenerators)
        val query = queryGenerators.foldIndexed("") { index, acc, queryGenerator ->
            if (index == 0) acc + queryGenerator.queryBuilderWrapper().query
            else acc + "AND" + queryGenerator.queryBuilderWrapper().query
        }
        println("query is $query")

        val arguments = arrayListOf<Any>()
        arguments.addAll(queryGenerators.flatMap { queryGenerator -> queryGenerator.queryBuilderWrapper().arguments})
        println("arguments are : $arguments")

        return QueryBuilderWrapper(query, arguments)
    }

}

/**
 * Query generator for date filter
 */
class DateFilterQueryGenerator(private val startDate: Date?, private val endDate: Date?) : QueryGenerator() {
    override fun queryBuilderWrapper(): QueryBuilderWrapper {
        var query = ""
        val arguments = ArrayList<Any>()

        val dateConverter = DateConverter()
        val sd = startDate?.let { dateConverter.fromDate(it) }
        val ed = endDate?.let { dateConverter.fromDate(it) }

        if ((sd != null) and (ed != null)) {
            query += " (date BETWEEN ? AND ?) "
            arguments.add(sd!!)
            arguments.add(ed!!)
        } else if (sd != null) {
            query += " (date >= ?) "
            arguments.add(sd)
        } else if (ed != null){
            query += " (date <= ?) "
            arguments.add(ed)
        }

        return QueryBuilderWrapper(query, arguments)
    }
}


/**
 * Query generator to search by categories
 */
class CategoryFilterQueryGenerator(private val categories: List<String>): QueryGenerator() {
    override fun queryBuilderWrapper(): QueryBuilderWrapper {
        val arguments = ArrayList<Any>()

        val str = categories.foldIndexed("") { index, acc, element ->
            arguments.add(element)
            if (index == 0) "?" else "$acc, ?"
        }
        val query = " (category IN ($str))"
        return QueryBuilderWrapper(query, arguments)
    }

}


/**
 * Query generator to search by keywords
 */
class KeywordFilterQueryGenerator(private val keyword: String): QueryGenerator() {
    override fun queryBuilderWrapper(): QueryBuilderWrapper {
        val arguments = ArrayList<Any>()

        val query = " ((description LIKE ?) OR (category LIKE ?)) "
        arguments.add("%$keyword%")
        arguments.add("%$keyword%")

        return QueryBuilderWrapper(query, arguments)
    }
}
