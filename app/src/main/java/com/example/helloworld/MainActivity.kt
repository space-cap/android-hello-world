package com.example.helloworld

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.helloworld.ui.theme.HelloWorldTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// 앱의 진입점(Entry Point)인 메인 액티비티 클래스입니다.
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 전체 화면(Edge-to-Edge) 모드를 활성화하여 시스템 바 영역까지 콘텐츠를 표시합니다.
        enableEdgeToEdge()
        // Jetpack Compose UI를 설정합니다.
        setContent {
            HelloWorldTheme {
                // Scaffold는 기본적인 머티리얼 디자인 레이아웃 구조를 제공합니다.
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(name = "Android", modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

// 화면에 인사말을 표시하는 컴포저블 함수입니다.
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

    Column(modifier = modifier) {
        Text(text = "Hello World")
        Text(text = "Current Time: $currentTime")
    }
}

// Android Studio의 미리보기(Preview) 창에서 UI를 확인할 수 있게 해주는 함수입니다.
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    HelloWorldTheme { Greeting("Android") }
}
