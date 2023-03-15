package com.example.espspecialisthelper

import android.annotation.SuppressLint
import android.content.Context
import android.os.*
import android.text.Html
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.espspecialisthelper.databinding.ActivityMainBinding
import com.example.espspecialisthelper.ui.about.About
import com.example.espspecialisthelper.ui.comissioning.Comissioning
import com.example.espspecialisthelper.ui.menu1.FragmentMenu1
import com.example.espspecialisthelper.ui.menu2.FragmentMenu2
import com.example.espspecialisthelper.ui.settings.Settings
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.*


class MainActivity : AppCompatActivity() {

    companion object {
        const val TAB_M1 = "m1"
        const val TAB_M2 = "m2"
        const val TAB_M3 = "m3"
        const val TAB_M4 = "m4"
        const val CHANGE_THEME = true
        const val NOTHING = false
        var reStartval = NOTHING
        var wasPause = false

        /**
         * Лябда для создания вибрации при взаимодействии с элементами интерфейса
         */
        val vibro: (Context) -> Unit = { context ->
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= 26) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        5,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else vibrator.vibrate(5)
        }

        /**
         * Лябда для вызова диалогового фрагмента для вывода справочной информации
         * Основные параметры для использования:
         * 1 - Текст сообщения
         * 2 - Контекст
         * Не обязательный параметр titleString - по умолчанию значение "Справка"
         * Не обязательны параметр negativeButtonSet - по умолчанию false. Необходим в случае, если необходимо
         * установить вторую негативную кнопку
         * Не обязательный параметр callback - по умолчанию пусто. Данный параметр
         * можно передать в функцию, если небходимо произвести какие-либо действия в месте вызова
         * после нажатия на кнопки диалога
         */
        @SuppressLint("RestrictedApi")
        fun dialogCaller(
            text: String, context: Context,
            titleString: String = context.getString(R.string.dialog_title),
            negativeButtonSet: Boolean = false,
            callback: ((Boolean) -> Unit)? = null
        ) {
            vibro(context)
            val mAlertDialog = AlertDialog.Builder(context)
            mAlertDialog.setTitle(titleString)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mAlertDialog.setMessage(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY))
            }
            mAlertDialog.setPositiveButton("OK") { dialog, _ ->
                /*если пользователь передал колбэк, передаем ему результат выбора в данном диалоговом окне*/
                callback?.let { it(true) }
                dialog.dismiss()
            }
            /*если пользователь хочет установить негативную кнопку, отображаем её*/
            if (negativeButtonSet) {
                mAlertDialog.setNegativeButton(context.getString(R.string.cancel_title)) { dialog, _ ->
                    /*если пользователь передал колбэк, передаем ему результат выбора в данном диалоговом окне*/
                    callback?.let { it(false) }
                    dialog.dismiss()
                }
            }
            mAlertDialog.show()
        }

    }

    private lateinit var binding: ActivityMainBinding
    private var mStack = HashMap<String, Stack<Fragment>>()
    private var mCurrentTab: String = ""
    private var initStatus = false
    private var isThemeSeted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)

        //получаем интент, который может быть создан при работающем сервисе отсчета времени ВНР
        val checkServiceIntent = intent.getBooleanExtra("isServiseStarted", false)

        /*данная проверка исключает двойную смену темы, если была команда на изменение из фрагмента настроек
        * также, если тема приложения не совпадает с системной, то произойдет двойной перезапуск активити, что не
        * желательно для улучшения производительности*/
        if (!reStartval && !isThemeSeted) {
            /*настраиваем тему приложения*/
            when (prefs.getString("appTheme", Settings.SYSTEM)) {
                Settings.LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                Settings.DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                Settings.SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
            isThemeSeted =
                true // делаем отметку, что функция по изменению темы уже была использована
        } else {
            isThemeSeted =
                false //после пропуска данного блока возвращаем состояние изменение темы в исходное состояние
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /*создаем массив из стэков, согласно общему количеству пунков в меню
         В нашем случае у нас 4 пункта меню, соответственно будем хранить 4-е стека с последовательностями
         дочерних фрагментов
          */
        mStack[TAB_M1] = Stack<Fragment>()
        mStack[TAB_M2] = Stack<Fragment>()
        mStack[TAB_M3] = Stack<Fragment>()
        mStack[TAB_M4] = Stack<Fragment>()

        val navView: BottomNavigationView = binding.navView
        navView.itemIconTintList = null

        //устанавливаем слушателя фрагментов
        navView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.fragment1 -> {
                    if (initStatus) vibro(applicationContext)
                    selectedTab(TAB_M1)
                    true
                }
                R.id.fragment2 -> {
                    if (initStatus) vibro(applicationContext)
                    selectedTab(TAB_M2)
                    true
                }
                R.id.fragmentSettings -> {
                    if (initStatus) vibro(applicationContext)
                    selectedTab(TAB_M3)
                    true
                }
                R.id.fragmentAbout -> {
                    if (initStatus) vibro(applicationContext)
                    selectedTab(TAB_M4)
                    true
                }

                else -> false
            }
        }

        if (reStartval) {
            reStartval = NOTHING
        } else {
            /*если активность была вызвана при нажатии на уведомление в шторке службы сервиса отсчета времени
            * то должен быть загружен фрагмент ВНР, в противном случае должен быть открыт фрагмент №1 по умолчанию*/
            if (checkServiceIntent) {
                navView.selectedItemId = R.id.fragment2
                pushFragments(TAB_M2, Comissioning(), true)
            } else {
                navView.selectedItemId = R.id.fragment1
            }
        }

        Log.d("3", "created")

    }

    /*Метод, который определяет к какому из 4-х стеков фрагментов меню
    добавить новый фрагмент. Врагменты, вызываемые из под главных фрагментов, должны попасть в
    соответствующий стек родительского фрагмента
     */
    private fun selectedTab(tabId: String) {
        mCurrentTab = tabId
        /*
       *    Если главный фрагмент отсутствует в массиве стеков, помещаем его туда.
       *    Отказываемся от анимации.
       *    Если главный фрагмент уже есть в массиве стеков, то помещаем последущие фрагменты,
       *    Вызываемые из под него в его стэк
       */
        if (mStack[tabId]!!.size == 0) {
            when (tabId) {
                TAB_M1 -> pushFragments(tabId, FragmentMenu1(), true)
                TAB_M2 -> pushFragments(tabId, FragmentMenu2(), true)
                TAB_M3 -> pushFragments(tabId, Settings(), true)
                TAB_M4 -> pushFragments(tabId, About(), true)
            }
        } else {
            pushFragments(tabId, mStack[tabId]!!.lastElement(), false)
        }
        if (!initStatus) initStatus = true
    }

    //метод, который загружает новый фрагмент на экран, после его помещения в стэк родительского фрагмента
    fun pushFragments(tag: String, fragment: Fragment, shoulAdd: Boolean) {
        if (shoulAdd) mStack[tag]!!.push(fragment)
        val manager = supportFragmentManager
        val ft = manager.beginTransaction()
        ft.replace(R.id.content, fragment)
        ft.commit()
    }

    //метод, обрабатывающий нажатие на кнопку НАЗАД
    @SuppressLint("CommitTransaction")
    fun popFragments() {
        val fragment = mStack[mCurrentTab]!!.elementAt(mStack[mCurrentTab]!!.size - 2)
        mStack[mCurrentTab]!!.pop()
        val manager = supportFragmentManager
        val ft = manager.beginTransaction()
        ft.replace(R.id.content, fragment)
        ft.commit()
    }

    //создаем собственную обработку нажатия на кнопку НАЗАД (перенаправление на собсвенный метод)
    override fun onBackPressed() {
        if (mStack[mCurrentTab]!!.size == 1) {
            // We are already showing first fragment of current tab, so when back pressed, we will finish this activity..
            finish()
            return
        }
        popFragments()
    }

    /*ниже реализация перехода на фрагмент при выходе активности из фонового режима*/
    override fun onPause() {
        super.onPause()
        wasPause = true
        Log.d("1", "paused")
    }

    /*при выходе из фонового режима проверяется наличие интента, которое может быть создано сервисной службой
    * отсчета времени ВНР. Если интент будет в наличии, то проверяется наличие фрагмента ВНР в массиве стеков и в
    * зависимости от этого будут предприняты различные меры по его активации на экране */
    override fun onResume() {
        super.onResume()
        if (wasPause) {
            /**При выходе из режима ожидания активности из уведомления сервисной службы отсчета времени ВНР
             * необходимо реализовать отображение фрагмента ВНР test
             * Если выход из режима ожидания не был инициирован из уведомления сервисной службы, то данную проверку нужно игнорировать*/
        }

        wasPause = false
        Log.d("2", "resumed")
    }

}