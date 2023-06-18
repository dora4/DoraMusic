package site.doramusic.app.base

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.InflateException
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.collection.ArrayMap
import androidx.core.view.LayoutInflaterCompat
import androidx.core.view.LayoutInflaterFactory
import androidx.databinding.ViewDataBinding
import com.lwh.jackknife.xskin.SkinLoader
import com.lwh.jackknife.xskin.SkinManager
import com.lwh.jackknife.xskin.attr.SkinAttr
import com.lwh.jackknife.xskin.attr.SkinAttrSupport
import com.lwh.jackknife.xskin.attr.SkinView
import com.lwh.jackknife.xskin.callback.ISkinChangedListener
import dora.BaseActivity
import site.doramusic.app.util.PreferencesManager
import java.lang.reflect.Constructor
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.*

abstract class BaseSkinActivity<T : ViewDataBinding> : BaseActivity<T>(), ISkinChangedListener, LayoutInflaterFactory {

    private val constructorArgs = arrayOfNulls<Any>(2)

    override fun onCreateView(parent: View?, name: String, context: Context, attrs: AttributeSet): View? {
        val delegate = delegate
        var view: View? = null
        try {
            // public View createView
            // (View parent, final String name, @NonNull Context context, @NonNull AttributeSet attrs)
            if (sCreateViewMethod == null) {
                val methodOnCreateView = delegate.javaClass.getMethod("createView", *sCreateViewSignature)
                sCreateViewMethod = methodOnCreateView
            }
            val obj = sCreateViewMethod!!.invoke(delegate, parent, name, context, attrs)
            if (obj != null) {
                view = obj as View
            }
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
        val skinAttrList = SkinAttrSupport.getSkinAttrs(attrs, context)
        if (skinAttrList.isEmpty()) {
            return view
        }
        if (view == null) {
            view = createViewFromTag(context, name, attrs)
        }
        injectSkin(view, skinAttrList)
        return view
    }

    /**
     * 获取主题色的颜色值。
     */
    protected fun getThemeColor() : Int {
        val prefsManager = PreferencesManager(this)
        val skinType = prefsManager.getSkinType()
        return if (skinType == 0) {
            val skinColor = prefsManager.getSkinColor()
            resources.getColor(skinColor)
        } else {
            val skinThemeColor = SkinLoader.getInstance().getColorRes("skin_theme_color")
            resources.getColor(skinThemeColor)
        }
    }

    private fun injectSkin(view: View?, skinAttrList: List<SkinAttr>) {
        if (skinAttrList.isNotEmpty()) {
            var skinViews = SkinManager.getInstance().getSkinViews(this)
            if (skinViews == null) {
                skinViews = ArrayList()
            }
            SkinManager.getInstance().addSkinView(this, skinViews)
            skinViews.add(SkinView(view, skinAttrList))
            if (SkinManager.getInstance().needChangeSkin()) {
                SkinManager.getInstance().apply(this)
            }
        }
    }

    private fun createViewFromTag(context: Context, name: String, attrs: AttributeSet): View? {
        var name = name
        if (name == "view") {
            name = attrs.getAttributeValue(null, "class")
        }
        return try {
            constructorArgs[0] = context
            constructorArgs[1] = attrs
            if (-1 == name.indexOf('.')) {
                // try the android.widget prefix first...
                createView(context, name, "android.widget.")
            } else {
                createView(context, name, null)
            }
        } catch (e: Exception) {
            // We do not want to catch these, lets return null and let the actual LayoutInflater
            null
        } finally {
            // Don't retain references on context.
            constructorArgs[0] = null
            constructorArgs[1] = null
        }
    }

    @Throws(InflateException::class)
    private fun createView(context: Context, name: String, prefix: String?): View? {
        var constructor = sConstructorMap[name]
        return try {
            if (constructor == null) {
                // Class not found in the cache, see if it's real, and try to add it
                val clazz = context.classLoader.loadClass(
                        if (prefix != null) prefix + name else name).asSubclass(View::class.java)
                constructor = clazz.getConstructor(*sConstructorSignature)
                sConstructorMap[name] = constructor
            }
            constructor!!.isAccessible = true
            constructor.newInstance(*constructorArgs)
        } catch (e: Exception) {
            // We do not want to catch these, lets return null and let the actual LayoutInflater
            null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val layoutInflater = LayoutInflater.from(this)
        LayoutInflaterCompat.setFactory(layoutInflater, this)
        super.onCreate(savedInstanceState)
        SkinManager.getInstance().addChangedListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        SkinManager.getInstance().removeChangedListener(this)
    }

    override fun onSkinChanged() {
        SkinManager.getInstance().apply(this)
    }

    companion object {
        val sConstructorSignature = arrayOf(Context::class.java, AttributeSet::class.java)
        private val sConstructorMap: MutableMap<String, Constructor<out View>?> = ArrayMap()
        private var sCreateViewMethod: Method? = null
        val sCreateViewSignature = arrayOf(View::class.java, String::class.java,
                Context::class.java, AttributeSet::class.java)
    }
}