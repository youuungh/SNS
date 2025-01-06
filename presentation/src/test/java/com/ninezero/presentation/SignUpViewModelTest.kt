package com.ninezero.presentation

import com.ninezero.domain.model.ApiResult
import com.ninezero.domain.usecase.UserUseCase
import com.ninezero.presentation.auth.signup.SignUpSideEffect
import com.ninezero.presentation.auth.signup.SignUpViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class SignUpViewModelTest {
    private lateinit var userUseCase: UserUseCase
    private lateinit var viewModel: SignUpViewModel

    private val testId = "test"
    private val testUsername = "Tester"
    private val testPassword = "123"

    @Before
    fun setup() {
        userUseCase = mock()
        viewModel = SignUpViewModel(userUseCase)
    }

    @Test
    fun `아이디 입력 시 상태 업데이트 테스트`() {
        // Given: 초기 상태 확인
        assertEquals("", viewModel.container.stateFlow.value.id)

        // When: 아이디 입력
        viewModel.onIdChange(testId)

        // Then: 상태 검증
        assertEquals(testId, viewModel.container.stateFlow.value.id)
    }

    @Test
    fun `사용자 이름 입력 시 상태 업데이트 테스트`() {
        assertEquals("", viewModel.container.stateFlow.value.username)
        viewModel.onUsernameChange(testUsername)
        assertEquals(testUsername, viewModel.container.stateFlow.value.username)
    }

    @Test
    fun `비밀번호 입력 시 상태 업데이트 테스트`() {
        assertEquals("", viewModel.container.stateFlow.value.password)
        viewModel.onPasswordChange(testPassword)
        assertEquals(testPassword, viewModel.container.stateFlow.value.password)
    }

    @Test
    fun `비밀번호 확인 불일치 시 에러 상태 테스트`() {
        viewModel.onPasswordChange(testPassword)
        viewModel.onPasswordConfirmChange("wrongPassword")
        assertTrue(viewModel.container.stateFlow.value.isPasswordConfirmError)
    }

    @Test
    fun `회원가입 버튼 활성화 조건 테스트`() {
        // When: 모든 필드 올바르게 입력
        viewModel.onIdChange(testId)
        viewModel.onUsernameChange(testUsername)
        viewModel.onPasswordChange(testPassword)
        viewModel.onPasswordConfirmChange(testPassword)

        // Then: 버튼 활성화 확인
        assertTrue(viewModel.container.stateFlow.value.isSignUpEnabled)
    }

    @Test
    fun `회원가입 성공 시 네비게이션 및 스낵바 테스트`() = runTest {
        // Given: UserUseCase mock 설정
        whenever(userUseCase.signUp(testId, testUsername, testPassword))
            .thenReturn(ApiResult.Success(true))

        // When: 회원가입 시도
        viewModel.onIdChange(testId)
        viewModel.onUsernameChange(testUsername)
        viewModel.onPasswordChange(testPassword)
        viewModel.onPasswordConfirmChange(testPassword)
        viewModel.onSignUpClick()

        // Then: SideEffect 검증
        val sideEffect = viewModel.container.sideEffectFlow.first()
        assertTrue(sideEffect is SignUpSideEffect.NavigateToLogin)

        // 다음 SideEffect 검증
        val nextSideEffect = viewModel.container.sideEffectFlow.first()
        assertTrue(nextSideEffect is SignUpSideEffect.ShowSnackbar)
        assertEquals(
            "회원가입에 성공했습니다",
            (nextSideEffect as SignUpSideEffect.ShowSnackbar).message
        )
    }

    @Test
    fun `회원가입 실패 시 에러 메시지 테스트`() = runTest {
        // Given: UserUseCase mock 설정
        val errorMessage = "회원가입 실패"
        whenever(userUseCase.signUp(testId, testUsername, testPassword))
            .thenReturn(ApiResult.Error.InvalidRequest(errorMessage))

        // When: 회원가입 시도
        viewModel.onIdChange(testId)
        viewModel.onUsernameChange(testUsername)
        viewModel.onPasswordChange(testPassword)
        viewModel.onPasswordConfirmChange(testPassword)
        viewModel.onSignUpClick()

        // Then: SideEffect 검증
        val sideEffect = viewModel.container.sideEffectFlow.first()
        assertTrue(sideEffect is SignUpSideEffect.ShowSnackbar)
        assertEquals(errorMessage, (sideEffect as SignUpSideEffect.ShowSnackbar).message)
    }
}