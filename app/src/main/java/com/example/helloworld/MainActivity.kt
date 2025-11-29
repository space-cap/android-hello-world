package com.example.helloworld

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.helloworld.ui.theme.HelloWorldTheme

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
                    composable("page1") {
                        Page1(navController = navController)
                    }
                    // "page2" 라우트에 대한 화면을 정의합니다.
                    composable("page2") {
                        Page2()
                    }
                }
            }
        }
    }
}

// 페이지 1 컴포저블
@Composable
fun Page1(navController: NavController, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "여기는 페이지 1입니다.")
        // 버튼을 클릭하면 "page2"로 이동합니다.
        Button(onClick = { navController.navigate("page2") }) {
            Text(text = "페이지 2로 이동")
        }
    }
}

// 페이지 2 컴포저블
@Composable
fun Page2(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "여기는 페이지 2입니다.")
    }
}

@Preview(showBackground = true)
@Composable
fun Page1Preview() {
    HelloWorldTheme {
        Page1(navController = rememberNavController())
    }
}

@Preview(showBackground = true)
@Composable
fun Page2Preview() {
    HelloWorldTheme {
        Page2()
    }
}
