package com.union.hora.business.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.union.hora.R
import kotlinx.android.synthetic.main.fragment_user_login.*
import kotlinx.android.synthetic.main.fragment_user_login.view.*

class UserLoginFragment(val action: LoginCallback) : Fragment(), View.OnClickListener {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_user_login, container, false)
        view.btn_login.setOnClickListener(this)
        view.tv_signUp.setOnClickListener(this)
        return view
    }

    interface LoginCallback {
        fun onSignUpClick()
        fun onLoginClick(username: String, password: String)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_login -> login()
            R.id.tv_signUp -> action.onSignUpClick()
        }
    }

    private fun login() {
        val username: String = et_username.text.toString()
        val password: String = et_password.text.toString()

        if (username.isEmpty()) {
            et_username.error = getString(R.string.username_not_empty)
            return
        }
        if (password.isEmpty()) {
            et_password.error = getString(R.string.password_not_empty)
            return
        }
        action.onLoginClick(username, password)
    }
}