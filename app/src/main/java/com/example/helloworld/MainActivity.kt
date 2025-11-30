package com.example.helloworld

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.helloworld.ui.theme.HelloWorldTheme
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import org.json.JSONObject

// 사용자 정보를 담는 data class
data class UserInfo(
        val id: String = "",
        val pw: String = "",
        val name: String = "",
        val phoneNum: String = ""
) {
    // JSON 문자열로 변환
    fun toJson(): String {
        val json = JSONObject()
        json.put("id", id)
        json.put("pw", pw)
        json.put("name", name)
        json.put("phoneNum", phoneNum)
        return json.toString()
    }

    companion object {
        // JSON 문자열에서 UserInfo 객체 생성
        fun fromJson(jsonString: String): UserInfo {
            return try {
                val json = JSONObject(jsonString)
                UserInfo(
                        id = json.optString("id", ""),
                        pw = json.optString("pw", ""),
                        name = json.optString("name", ""),
                        phoneNum = json.optString("phoneNum", "")
                )
            } catch (e: Exception) {
                UserInfo()
            }
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelloWorldTheme {
                // NavController 인스턴스를 생성하고 기억합니다.
                val navController = rememberNavController()

                // NavHost를 설정하고 시작 화면을 "page1"로 지정합니다.
                NavHost(navController = navController, startDestination = "page1") {
                    // "page1" 라우트에 대한 화면을 정의합니다.
                    composable("page1") { Page1(navController = navController) }
                    // "page2/{userInfo}" 라우트에 대한 화면을 정의합니다. (파라미터 포함)
                    composable(
                            route = "page2/{userInfo}",
                            arguments =
                                    listOf(navArgument("userInfo") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val encodedUserInfo = backStackEntry.arguments?.getString("userInfo") ?: ""
                        val decodedJson =
                                URLDecoder.decode(
                                        encodedUserInfo,
                                        StandardCharsets.UTF_8.toString()
                                )
                        val userInfo = UserInfo.fromJson(decodedJson)
                        Page2(userInfo = userInfo)
                    }
                }
            }
        }
    }
}

// 페이지 1 컴포저블
@Composable
fun Page1(navController: NavController, modifier: Modifier = Modifier) {
    // 각 TextField의 입력값을 저장하는 state들
    var id by remember { mutableStateOf("") }
    var pw by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phoneNum by remember { mutableStateOf("") }

    Column(
            modifier = modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "회원 정보 입력", fontSize = 24.sp)

        Spacer(modifier = Modifier.height(24.dp))

        // ID 입력 필드
        OutlinedTextField(
                value = id,
                onValueChange = { id = it },
                label = { Text("아이디") },
                placeholder = { Text("아이디를 입력하세요") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 비밀번호 입력 필드
        OutlinedTextField(
                value = pw,
                onValueChange = { pw = it },
                label = { Text("비밀번호") },
                placeholder = { Text("비밀번호를 입력하세요") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 이름 입력 필드
        OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("이름") },
                placeholder = { Text("이름을 입력하세요") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 전화번호 입력 필드
        OutlinedTextField(
                value = phoneNum,
                onValueChange = { phoneNum = it },
                label = { Text("전화번호") },
                placeholder = { Text("010-1234-5678") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 버튼을 클릭하면 입력한 정보와 함께 "page2"로 이동합니다.
        Button(
                onClick = {
                    // UserInfo 객체 생성
                    val userInfo = UserInfo(id = id, pw = pw, name = name, phoneNum = phoneNum)
                    // JSON으로 변환 후 URL 인코딩
                    val jsonString = userInfo.toJson()
                    val encodedUserInfo =
                            URLEncoder.encode(jsonString, StandardCharsets.UTF_8.toString())
                    navController.navigate("page2/$encodedUserInfo")
                },
                enabled =
                        id.isNotEmpty() &&
                                pw.isNotEmpty() &&
                                name.isNotEmpty() &&
                                phoneNum.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
        ) { Text(text = "페이지 2로 이동") }
    }
}

// 페이지 2 컴포저블
@Composable
fun Page2(userInfo: UserInfo = UserInfo(), modifier: Modifier = Modifier) {
    Column(
            modifier = modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "입력한 정보 확인", fontSize = 24.sp)

        Spacer(modifier = Modifier.height(24.dp))

        // Page1에서 전달받은 정보를 표시
        InfoRow(label = "아이디", value = userInfo.id)
        Spacer(modifier = Modifier.height(12.dp))

        InfoRow(label = "비밀번호", value = "•".repeat(userInfo.pw.length))
        Spacer(modifier = Modifier.height(12.dp))

        InfoRow(label = "이름", value = userInfo.name)
        Spacer(modifier = Modifier.height(12.dp))

        InfoRow(label = "전화번호", value = userInfo.phoneNum)
    }
}

// 정보를 표시하는 헬퍼 컴포저블
@Composable
fun InfoRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, fontSize = 14.sp)
        Text(text = value, fontSize = 18.sp)
    }
}

@Preview(showBackground = true)
@Composable
fun Page1Preview() {
    HelloWorldTheme { Page1(navController = rememberNavController()) }
}

@Preview(showBackground = true)
@Composable
fun Page2Preview() {
    HelloWorldTheme { Page2() }
}
