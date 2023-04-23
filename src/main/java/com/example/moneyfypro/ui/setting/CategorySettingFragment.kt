package com.example.moneyfypro.ui.setting

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moneyfypro.R
import com.example.moneyfypro.databinding.AddCategoryDialogBinding
import com.example.moneyfypro.databinding.FragmentCategorySettingBinding
import com.example.moneyfypro.model.SettingViewModel
import com.example.moneyfypro.utils.CategoryPreferenceManager
import com.example.moneyfypro.utils.ExpensesPreferencesManager
import com.google.android.material.snackbar.Snackbar
import java.util.*

class CategorySettingFragment : DialogFragment(), ItemOnTouchListener{
    private lateinit var categorySettingFragmentBinding: FragmentCategorySettingBinding
    private val settingViewModel: SettingViewModel by activityViewModels()
    private lateinit var catAdapter: CategoryItemsAdapter
    private var lastSnackBar: Snackbar? = null


    companion object {
        const val TAG = "Category setting"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_MoneyfyPro)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        categorySettingFragmentBinding =
            FragmentCategorySettingBinding.inflate(inflater, container, false)
        return categorySettingFragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        catAdapter = CategoryItemsAdapter()
        val catItemTouchHelper =
            ItemTouchHelper(CategoryItemTouchHelperCallback(this))

        categorySettingFragmentBinding.apply {
            categoryOptionItems.adapter = catAdapter
            catItemTouchHelper.attachToRecyclerView(categoryOptionItems)
            categoryOptionItems.layoutManager = LinearLayoutManager(activity)
            addNewButton.setOnClickListener { addNewCategoryDialog() }
        }

        settingViewModel.saveCategories.observe(viewLifecycleOwner) {
            catAdapter.submitList(it.toList())
        }
        configureReturnCallback()
    }

    private fun configureReturnCallback() {
        val toolbar = categorySettingFragmentBinding.toolbar
        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        toolbar.setNavigationOnClickListener { dismiss() }
    }

    private fun addNewCategoryDialog() {
        AddNewCategoryDialog {updateCategoryPreferences()}.show(
            requireActivity().supportFragmentManager,
            AddNewCategoryDialog.TAG
        )
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        val from = viewHolder.adapterPosition
        val to = target.adapterPosition
        val adapter = recyclerView.adapter as? CategoryItemsAdapter
        adapter?.let {
            val currentList = it.currentList.toMutableList()
            Collections.swap(currentList, from, to)
            settingViewModel.setCategories(currentList.toSet())
            updateCategoryPreferences()
        }
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        if (direction == ItemTouchHelper.RIGHT) {
            val currentList = settingViewModel.saveCategories.value ?: return
            if (currentList.size == 1) {
                catAdapter.notifyItemChanged(0)
                showSnackBar("Can't delete because there is only 1 left!")
                return
            }
            showSnackBar("Category is deleted")
            settingViewModel.removeCategory(currentList.elementAt(viewHolder.adapterPosition))
            updateCategoryPreferences()
        }
    }

    private fun updateCategoryPreferences() {
        val sharedPreferences = requireActivity().getSharedPreferences(ExpensesPreferencesManager.KEY,Context.MODE_PRIVATE)
        val manager = CategoryPreferenceManager(requireContext(), sharedPreferences)
        manager.editValue(sharedPreferences.setToString(settingViewModel.saveCategories.value ?: setOf()))
    }



    private fun showSnackBar(message: String) {
        if (lastSnackBar != null && lastSnackBar!!.isShown) lastSnackBar!!.dismiss()

        val snackBar = Snackbar.make(categorySettingFragmentBinding.root, message, 1000)
        snackBar.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        snackBar.setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.pale_pink))
        snackBar.show()
        lastSnackBar = snackBar
    }

    class CategoryItemTouchHelperCallback(
        private val listener: ItemOnTouchListener
    ) :
        ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.RIGHT
        ) {

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return listener.onMove(recyclerView, viewHolder, target)
        }


        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            return listener.onSwiped(viewHolder, direction)
        }
    }


    class AddNewCategoryDialog(private val updateCategoryPrefCallback: () -> Unit) : DialogFragment() {
        private lateinit var binding: AddCategoryDialogBinding
        private val settingViewModel: SettingViewModel by activityViewModels()


        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            binding = AddCategoryDialogBinding.inflate(layoutInflater, null, false)
            val builder = AlertDialog.Builder(requireActivity())
            // Since the dialog is wrapped inside a fragment, we should set cancelable on fragment.
            isCancelable = false
            return builder.setView(binding.root)
                .setTitle("Add New Category")
                .setPositiveButton("Add") { _, _ ->
                    addNewCategory()
                    dismiss()
                }
                .setNegativeButton("Cancel") { _, _ -> dismiss() }
                .create()
        }

        private fun addNewCategory() {
            val newCategory = binding.addCategoryField.text ?: return
            settingViewModel.addCategory(newCategory.toString())
            updateCategoryPrefCallback()
        }

        companion object {
            const val TAG = "Add Category Dialog"
        }
    }


}