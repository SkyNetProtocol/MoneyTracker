package com.example.loginapp.domain.usecase

import app.cash.turbine.test
import com.example.loginapp.MainDispatcherRule
import com.example.loginapp.TestData
import com.example.loginapp.common.Result
import com.example.loginapp.domain.repository.UserRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginUseCaseTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var userRepository: UserRepository
    private lateinit var loginUseCase: LoginUseCase

    @Before
    fun setup() {
        userRepository = mockk()
        loginUseCase = LoginUseCase(userRepository)
    }

    @Test
    fun `when user exists and password matches should return success with user`() = runTest {
        // Given
        val username = "testuser"
        val password = "password123"
        val user = TestData.testUser.copy(passwordHash = password)
        every { userRepository.getUserByUsername(username) } returns flowOf(Result.Success(user))

        // When
        val result = loginUseCase(username, password)

        // Then
        result.test {
            val item = awaitItem()
            assertThat(item).isInstanceOf(Result.Success::class.java)
            assertThat((item as Result.Success).data).isEqualTo(user)
            awaitComplete()
        }
    }

    @Test
    fun `when user exists but password does not match should return success with null`() = runTest {
        // Given
        val username = "testuser"
        val correctPassword = "password123"
        val wrongPassword = "wrongpassword"
        val user = TestData.testUser.copy(passwordHash = correctPassword)
        every { userRepository.getUserByUsername(username) } returns flowOf(Result.Success(user))

        // When
        val result = loginUseCase(username, wrongPassword)

        // Then
        result.test {
            val item = awaitItem()
            assertThat(item).isInstanceOf(Result.Success::class.java)
            assertThat((item as Result.Success).data).isNull()
            awaitComplete()
        }
    }

    @Test
    fun `when user does not exist should return success with null`() = runTest {
        // Given
        val username = "nonexistent"
        val password = "password123"
        every { userRepository.getUserByUsername(username) } returns flowOf(Result.Success(null))

        // When
        val result = loginUseCase(username, password)

        // Then
        result.test {
            val item = awaitItem()
            assertThat(item).isInstanceOf(Result.Success::class.java)
            assertThat((item as Result.Success).data).isNull()
            awaitComplete()
        }
    }

    @Test
    fun `when repository returns error should propagate error`() = runTest {
        // Given
        val username = "testuser"
        val password = "password123"
        val exception = Exception("Database error")
        every { userRepository.getUserByUsername(username) } returns flowOf(Result.Error(exception))

        // When
        val result = loginUseCase(username, password)

        // Then
        result.test {
            val item = awaitItem()
            assertThat(item).isInstanceOf(Result.Error::class.java)
            assertThat((item as Result.Error).exception).isEqualTo(exception)
            awaitComplete()
        }
    }

    @Test
    fun `when repository returns loading should propagate loading`() = runTest {
        // Given
        val username = "testuser"
        val password = "password123"
        every { userRepository.getUserByUsername(username) } returns flowOf(Result.Loading)

        // When
        val result = loginUseCase(username, password)

        // Then
        result.test {
            val item = awaitItem()
            assertThat(item).isInstanceOf(Result.Loading::class.java)
            awaitComplete()
        }
    }
}
