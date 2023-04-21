package com.example.moneyfypro.ui

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface.OnMultiChoiceClickListener
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.moneyfypro.R
import com.example.moneyfypro.data.Expense
import com.example.moneyfypro.data.adjustedDateWithoutTimeZone
import com.example.moneyfypro.data.dateFormat
import com.example.moneyfypro.databinding.ActivityMainBinding
import com.example.moneyfypro.model.ExpensesViewModel
import com.example.moneyfypro.model.FilterViewModel
import com.example.moneyfypro.model.SettingViewModel
import com.example.moneyfypro.ui.custom_view.DraggableFloatingActionButton
import com.example.moneyfypro.ui.setting.*
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), DraggableFloatingActionButton.OnClickListener {
    private lateinit var navController: NavController
    lateinit var binding: ActivityMainBinding
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private val filterViewModel: FilterViewModel by viewModels()
    private val expensesViewModel: ExpensesViewModel by viewModels()
    private val settingViewModel: SettingViewModel by viewModels()
    private var exitSnackBar: Snackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // If I put set default night mode after onCreate super, onCreate will be called again...
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        super.onCreate(savedInstanceState)
        initSetting()
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        navControllerListenerSetup()

//        val appConfig = AppBarConfiguration(
//            setOf(
//                R.id.expensesSummaryFragment,
//                R.id.expensesHistoryFragment,
//                R.id.expensesLineChartFragment,
//                R.id.categorySettingFragment
//            )
//        )
//
//        // This setup the action bar so that the bar title fit the page.
//        setupActionBarWithNavController(navController, appConfig)

        binding.apply {
            // Navigation view provide interface for us to handle the navigation.
            // Just make sure menu items are setup with matched id.
            bottomNavBar.setupWithNavController(navController)

            dateFilter.apply {
                calenderButtonFrom.setOnClickListener {
                    onSelectCalender(isStartDay = true)
                }
                calenderButtonTo.setOnClickListener {
                    onSelectCalender(isStartDay = false)
                }
            }
            searchFilter.apply {
                searchText.addTextChangedListener(SearchTextWatcher(this@MainActivity))
                searchLayout.setEndIconOnClickListener {
                    if (searchText.text.isNullOrEmpty()) {
                        if (binding.dateFilter.root.visibility == View.GONE) {
                            binding.searchFilter.root.visibility = View.GONE
                            binding.dateFilter.root.visibility = View.VISIBLE
                        }
                        return@setEndIconOnClickListener
                    }

                    searchText.text?.clear()
                }
            }
        }

        setDrawer()
//        configureFAB()
        setCalenderFilterListener()
        backPressSetup()
    }



    private fun backPressSetup() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (exitSnackBar != null && exitSnackBar!!.isShown) {
                    finish()
                    return
                }

                val snackBar = Snackbar.make(binding.root, "Press again to exit.", 1000)
                snackBar.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.white))
                snackBar.setBackgroundTint(ContextCompat.getColor(this@MainActivity, R.color.black))
                snackBar.show()

                exitSnackBar = snackBar
            }
        })
    }


    private fun initSetting() {
        val sharedPreferences = getSharedPreferences("share", Context.MODE_PRIVATE) ?: return
        initCategorySetting(sharedPreferences)
        initCurrencySetting(sharedPreferences)

    }

    private fun initCategorySetting(sharedPreferences: SharedPreferences) {
        val catsString = sharedPreferences.getString(
            sharedPreferences.categoryId(),
            sharedPreferences.setToString(sharedPreferences.defaultCategories(this))
        )
        catsString?.let { settingViewModel.setCategories(sharedPreferences.stringToSet(catsString)) }
    }

    private fun initCurrencySetting(sharedPreferences: SharedPreferences) {
        val currency = sharedPreferences.getString(
            sharedPreferences.currencyId(),
            sharedPreferences.defaultCurrency()
        )
        currency?.let {
            settingViewModel.setCurrency(currency)
        }
    }

    private fun setDrawer() {
        val drawerView = binding.drawerLayout
//        binding.drawer.setupWithNavController(navController)


        // add listener to the drawerLayout
        drawerToggle =
            ActionBarDrawerToggle(this, drawerView, R.string.drawer, R.string.close_Drawer)


        // Sync the drawer state so that the appbar can adjust its label based on the state of drawer (open or not)
        drawerToggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.drawer.root.setNavigationItemSelectedListener { item ->
            if (item.itemId == R.id.category_setting_navigator) {
                CategorySettingFragment().show(supportFragmentManager, CategorySettingFragment.TAG)
                return@setNavigationItemSelectedListener true
            }

            if (item.itemId == R.id.currency_setting_navigator) {
                CurrencySettingFragment().show(supportFragmentManager, CurrencySettingFragment.TAG)
                return@setNavigationItemSelectedListener true
            }
            false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.filter_dropdown, menu)

        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (drawerToggle.onOptionsItemSelected(item)) return true

        when (item.itemId) {

            R.id.date_filter_menu_item -> {
                if (binding.dateFilter.root.visibility == View.GONE) {
                    binding.searchFilter.root.visibility = View.GONE
                    binding.dateFilter.root.visibility = View.VISIBLE
                }
                return true
            }

            R.id.search_filter_menu_item -> {
                if (binding.searchFilter.root.visibility == View.GONE) {
                    binding.dateFilter.root.visibility = View.GONE
                    binding.searchFilter.root.visibility = View.VISIBLE
                }
                return true
            }

            R.id.category_filter_menu_item -> {
                showCategoryFilterDialog()
                return true
            }

            else -> return super.onOptionsItemSelected(item)

        }
    }


    private fun showCategoryFilterDialog() {
        CategoryFilterDialogFragment(
            expensesViewModel,
            settingViewModel,
            filterViewModel
        ).show(supportFragmentManager, CategoryFilterDialogFragment.TAG)
    }


    private fun setCalenderFilterListener() {
        filterViewModel.apply {
            startDate.observe(this@MainActivity) {
                binding.dateFilter.startDayFilterText.text = Expense.dateFormat().format(it)
                expensesViewModel.setExpensesFilter(allFilters())
            }
            endDate.observe(this@MainActivity) {
                binding.dateFilter.endDateFilterText.text = Expense.dateFormat().format(it)
                expensesViewModel.setExpensesFilter(allFilters())
            }
        }
    }


    /**
     * Open up dialog for choosing calender
     */
    private fun onSelectCalender(isStartDay: Boolean) {
        // Calender constraint
        val calender = Calendar.getInstance()
        val validator = filterViewModel.dateValidator(isStartDay)
        var calenderConstraintBuilder = CalendarConstraints.Builder().setOpenAt(
            calender.timeInMillis
        )
        if (validator != null) calenderConstraintBuilder =
            calenderConstraintBuilder.setValidator(validator)

        // Set picker
        val picker = MaterialDatePicker.Builder.datePicker().setTitleText(
            if (isStartDay) "Choose Start Date"
            else "Choose End Date"
        ).setCalendarConstraints(calenderConstraintBuilder.build()).build()
        picker.addOnPositiveButtonClickListener {
            setDateFilter(isStartDay, Date(it))
        }

        picker.show(this.supportFragmentManager, "materialDatePicker")
    }

    private fun setDateFilter(isStartDay: Boolean, date: Date) {
        val adjustedDate = Expense.adjustedDateWithoutTimeZone(date) ?: return

        if (isStartDay) {
            filterViewModel.setStartData(adjustedDate)
        } else {
            filterViewModel.setEndDate(adjustedDate)
        }
    }

//
//    /**
//     * Configuration for floating action button
//     */
//    private fun configureFAB() {
//        binding.fab.apply {
//            setOnClickListener {
////                navController.navigate(R.id.addExpensesFragment)
//                AddExpensesFragment().show(supportFragmentManager, AddExpensesFragment.TAG)
//            }
//        }
//    }

    override fun nonDragFABClick() {
        AddExpensesFragment().show(supportFragmentManager, AddExpensesFragment.TAG)
    }


    /**
     * Navigation up.
     */
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() or super.onSupportNavigateUp()
    }

    /**
     * Set navController destination change listener
     */
    private fun navControllerListenerSetup() {
        navController.addOnDestinationChangedListener { _, destination, _
            ->
            run {
                Log.d("nav", destination.toString())
                val visibility = if (isOnMenuFragment(destination.id)) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

                binding.fab.visibility = visibility
                binding.bottomNavBar.visibility = visibility
            }
        }
    }

    /**
     * Check if current fragment is one of the 3 fragments
     */
    private fun isOnMenuFragment(destinationId: Int): Boolean {
        return listOf(
            R.id.expensesSummaryFragment,
            R.id.expensesHistoryFragment,
            R.id.expensesLineChartFragment
        ).contains(destinationId)
    }

    override fun onResume() {
        super.onResume()
        // Update the UI in case expenses data changed.
        expensesViewModel.invalidate()
    }


    /**
     * Category filter dialog
     */
    class CategoryFilterDialogFragment(
        private val expensesViewModel: ExpensesViewModel,
        private val settingViewModel: SettingViewModel,
        private val filterViewModel: FilterViewModel
    ) : DialogFragment() {
        private val _selectedCategories = mutableListOf<String>()

        init {
            filterViewModel.selectedCategories.value?.let { _selectedCategories.addAll(it.toList()) }
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val cats = categories()
            val categoryStates = categoriesStates()
            val builder = AlertDialog.Builder(activity)
                .setTitle("Category Filter")
                .setMultiChoiceItems(
                    cats,
                    categoryStates,
                    multiChoicesClickListener(cats, categoryStates)
                )
                .setPositiveButton("Apply") { _, _ ->
                    setCategoryFilter()
                }
                .setNegativeButton("Cancel") { _, _ -> dismiss() }


            return builder.create()
        }


        private fun multiChoicesClickListener(
            cats: Array<String>,
            categoryStates: BooleanArray
        ): OnMultiChoiceClickListener {
            return OnMultiChoiceClickListener setMultiChoiceItems@{ dialog, index, isChecked ->
                val alertDialog = dialog as AlertDialog
                val listView = alertDialog.listView


                val catName = cats[index]
                // If nothing else is selected, this cannot be unselected!
                if (listView.checkedItemCount == 0) {
                    listView.setItemChecked(index, true)
                    return@setMultiChoiceItems
                }

                // If none is selected, then the others must be unselected.
                if ((catName == "None") and isChecked) {
                    categories().forEachIndexed { pos, _ ->
                        if (cats[pos] != "None") {
                            categoryStates[pos] = false
                            listView.setItemChecked(pos, false)
                        }
                    }
                    _selectedCategories.clear()
                } else if ((catName != "None") and isChecked and (listView.checkedItemPositions[cats.indexOf(
                        "None"
                    )])
                ) {
                    val pos = cats.indexOf("None")
                    categoryStates[pos] = false
                    listView.setItemChecked(pos, false)
                    _selectedCategories.add(catName)
                } else if (isChecked) {
                    _selectedCategories.add(catName)
                } else {
                    _selectedCategories.remove(catName)
                }

            }
        }


        private fun categories(): Array<String> {
            val arr = arrayOf("None")
            return arr + (settingViewModel.saveCategories.value?.toTypedArray() ?: arrayOf())
        }

        private fun categoriesStates(): BooleanArray {
            val selectedCategories = filterViewModel.selectedCategories.value
            selectedCategories?.takeIf { it.isNotEmpty() }?.let {
                val states = categories().map { category ->
                    it.contains(category)
                }

                return states.toBooleanArray()
            } ?: return categories().map { category -> category == "None" }.toBooleanArray()
        }

        private fun setCategoryFilter() {
            filterViewModel.setCategoriesFilter(_selectedCategories)
            expensesViewModel.setExpensesFilter(filterViewModel.allFilters())
        }

        companion object {
            const val TAG = "category filter"
        }
    }

    class SearchTextWatcher(private val activity: MainActivity) : TextWatcher {
        private val job: Job? = null

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            job?.cancel()
            activity.lifecycleScope.launch {
                delay(500)
                val filterViewModel = activity.filterViewModel
                val expensesViewModel = activity.expensesViewModel

                filterViewModel.setKeyword(s.toString())
                expensesViewModel.setExpensesFilter(filterViewModel.allFilters())
            }

        }

        override fun afterTextChanged(s: Editable?) {

        }

    }


}