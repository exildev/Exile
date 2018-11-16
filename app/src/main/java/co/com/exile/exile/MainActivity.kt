package co.com.exile.exile

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout

import com.roughike.bottombar.BottomBar
import com.roughike.bottombar.OnTabSelectListener

import co.com.exile.exile.chat.ChatFragment
import co.com.exile.exile.profile.ProfileFragment
import co.com.exile.exile.report.ReportFragmetPagerAdapter
import co.com.exile.exile.task.TasksFragmetPagerAdapter
import shortbread.Shortcut

class MainActivity : AppCompatActivity() {

    private var tasksAdapter: TasksFragmetPagerAdapter? = null
    private var reportAdapter: ReportFragmetPagerAdapter? = null
    private var mTaskPager: ViewPager? = null
    private var mReportPager: ViewPager? = null
    private var fragmentContainer: FrameLayout? = null
    private var tabs: TabLayout? = null
    private var appBar: AppBarLayout? = null
    private var bottomBar: BottomBar? = null
    private var menu: Menu? = null
    private var chatFragment: ChatFragment? = null

    private val tabSelectListener = OnTabSelectListener { tabId ->
        supportActionBar?.show()
        appBar?.setExpanded(true)
        when (tabId) {
            R.id.navigation_tasks -> showViewPager(R.id.navigation_tasks)
            R.id.navigation_report -> showViewPager(R.id.navigation_report)
            R.id.navigation_chat -> showFragment(R.id.navigation_chat)
            R.id.navigation_profile -> {
                actionBar.hide()
                showFragment(R.id.navigation_profile)
            }
        }
    }

    private fun showFragment(fragment: Int) {
        tabs?.visibility = View.GONE

        val f: Fragment?

        if (fragment == R.id.navigation_chat) {
            chatFragment = ChatFragment()
            f = chatFragment
            showOption(R.id.nav_add)
        } else {
            f = ProfileFragment()
        }

        if (mTaskPager?.visibility == View.VISIBLE) {
            mTaskPager?.visibility = View.GONE
            fragmentContainer?.visibility = View.VISIBLE
        } else if (mReportPager?.visibility == View.VISIBLE) {
            mReportPager?.visibility = View.GONE
            fragmentContainer?.visibility = View.VISIBLE
        }

        val fragmentManager = supportFragmentManager
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, f)
                .commitNow()
    }

    private fun showViewPager(option: Int) {
        if (option == R.id.navigation_tasks) {
            if (tasksAdapter == null) {
                tasksAdapter = TasksFragmetPagerAdapter(supportFragmentManager)
                mTaskPager?.adapter = tasksAdapter
            }

            hideOption(R.id.nav_add)
            mTaskPager?.visibility = View.VISIBLE
            mReportPager?.visibility = View.GONE
            tabs?.setupWithViewPager(mTaskPager)
        } else if (option == R.id.navigation_report) {
            if (reportAdapter == null) {
                reportAdapter = ReportFragmetPagerAdapter(supportFragmentManager)
                mReportPager?.adapter = reportAdapter
            }

            showOption(R.id.nav_add)
            mTaskPager?.visibility = View.GONE
            mReportPager?.visibility = View.VISIBLE
            tabs?.setupWithViewPager(mReportPager)
        }

        fragmentContainer?.visibility = View.GONE
        tabs?.visibility = View.VISIBLE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        appBar = findViewById(R.id.appbar)
        mTaskPager = findViewById(R.id.task_pager)
        mReportPager = findViewById(R.id.report_pager)
        tabs = findViewById(R.id.tabs)
        tabs?.setupWithViewPager(mTaskPager)

        fragmentContainer = findViewById(R.id.fragment_container)

        bottomBar = findViewById(R.id.bottomBar)
        bottomBar?.setOnTabSelectListener(tabSelectListener)

        val chatTab = bottomBar?.getTabWithId(R.id.navigation_chat)
        chatTab?.setBadgeCount(5)

        startService(Intent(this, SocketService::class.java))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        this.menu = menu
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.nav_add) {

            when (bottomBar?.currentTabId) {
                R.id.navigation_report -> reportAdapter?.addReport(this)
                R.id.navigation_chat -> chatFragment?.addChat()
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == ReportFragmetPagerAdapter.ADD_NEW_REPORT) {
            val fragment = mReportPager?.adapter?.instantiateItem(mReportPager, mReportPager?.currentItem ?: 0) as Fragment
            fragment.onActivityResult(requestCode, resultCode, data)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun hideOption(id: Int) {
        if (menu != null) {
            val item = menu?.findItem(id)
            item?.isVisible = false
        }
    }

    private fun showOption(id: Int) {
        if (menu != null) {
            val item = menu?.findItem(id)
            item?.isVisible = true
        }
    }

    @Shortcut(id = "show_reports", icon = R.drawable.ic_report_24dp, shortLabel = "Reportes", backStack = [LoginActivity::class])
    fun showReports() {
        showViewPager(R.id.navigation_report)
    }
}
