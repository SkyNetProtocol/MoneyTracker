package com.example.loginapp.presentation.login

import app.cash.turbine.test
import com.example.loginapp.MainDispatcherRule
import com.example.loginapp.TestData
import com.example.loginapp.common.Result
import com.example.loginapp.domain.usecase.LoginUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var loginUseCase: LoginUseCase
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setup() {
        loginUseCase = mockk()
        viewModel = LoginViewModel(loginUseCase)
    }

    @Test
    fun `when login is successful should update state to success`() = runTest {
        // Given
        val username = "testuser"
        val password = "password123"
        val user = TestData.testUser
        every { loginUseCase(username, password) } returns flowOf(Result.Success(user))

        // When/Then
        viewModel.loginState.test {
            assertThat(awaitItem()).isInstanceOf(LoginState.Idle::class.java)
            
            viewModel.login(username, password)
            
            assertThat(awaitItem()).isInstanceOf(LoginState.Success::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when credentials are invalid should update state to error`() = runTest {
        // Given
        val username = "testuser"
        val password = "wrongpassword"
        every { loginUseCase(username, password) } returns flowOf(Result.Success(null))

        // When/Then
        viewModel.loginState.test {
            assertThat(awaitItem()).isInstanceOf(LoginState.Idle::class.java)

            viewModel.login(username, password)

            val item = awaitItem()
            assertThat(item).isInstanceOf(LoginState.Error::class.java)
            assertThat((item as LoginState.Error).message).isEqualTo("Invalid credentials")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when login use case returns error should update state to error`() = runTest {
        // Given
        val username = "testuser"
        val password = "password123"
        val errorMessage = "Network Error"
        every { loginUseCase(username, password) } returns flowOf(Result.Error(Exception(errorMessage)))

        // When/Then
        viewModel.loginState.test {
            assertThat(awaitItem()).isInstanceOf(LoginState.Idle::class.java)

            viewModel.login(username, password)

            val item = awaitItem()
            assertThat(item).isInstanceOf(LoginState.Error::class.java)
            assertThat((item as LoginState.Error).message).isEqualTo(errorMessage)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state should be idle`() = runTest {
        // Then
        viewModel.loginState.test {
            assertThat(awaitItem()).isInstanceOf(LoginState.Idle::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
