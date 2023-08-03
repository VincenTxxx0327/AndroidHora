package com.union.hora.business.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.union.hora.R
import kotlinx.android.synthetic.main.fragment_user_register.*
import kotlinx.android.synthetic.main.fragment_user_register.view.*

class UserRegisterFragment(val action: RegisterCallback) : Fragment(), View.OnClickListener {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_user_register, container, false)
        view.btn_register.setOnClickListener(this)
        view.tv_signIn.setOnClickListener(this)
        return view
    }

    interface RegisterCallback {
        fun onSignInClick()
        fun onRegisterClick(username: String, password: String)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_register -> register()
            R.id.tv_signIn -> action.onSignInClick()
        }
    }

    private fun register() {
        val username: String = et_username.text.toString().trim()
        val password: String = et_password.text.toString().trim()
        val password2: String = et_password2.text.toString().trim()
        if (username.isEmpty()) {
            et_username.error = getString(R.string.username_not_empty)
            return
        }
        if (password.isEmpty()) {
            et_password.error = getString(R.string.password_not_empty)
            return
        }
        if (password2.isEmpty()) {
            et_password2.error = getString(R.string.confirm_password_not_empty)
            return
        }
        if (password != password2) {
            et_password2.error = getString(R.string.password_cannot_match)
            return
        }
        action.onRegisterClick(username, password)
    }
}