# 실무용 Composable Button 가이드

> **작성일**: 2024-12-05  
> **난이도**: ⭐⭐⭐ 중급  
> **예상 학습 시간**: 3-4시간

## 목차
1. [개요](#개요)
2. [기본 Button 이해하기](#기본-button-이해하기)
3. [실무 Button 설계 원칙](#실무-button-설계-원칙)
4. [재사용 가능한 Button 구현](#재사용-가능한-button-구현)
5. [고급 패턴](#고급-패턴)
6. [접근성과 모범 사례](#접근성과-모범-사례)
7. [실전 예제](#실전-예제)

---

## 개요

실무에서 Button은 가장 많이 사용하는 UI 컴포넌트 중 하나입니다. 하지만 단순히 `Button { }`을 사용하는 것을 넘어, **재사용 가능하고**, **확장 가능하며**, **접근성**을 고려한 Button을 설계하는 것이 중요합니다.

### 이 가이드에서 배울 내용
- ✅ Jetpack Compose의 기본 Button 컴포넌트 이해
- ✅ 디자인 시스템에 맞는 커스텀 Button 설계
- ✅ 다양한 스타일과 상태를 처리하는 Button 구현
- ✅ 접근성을 고려한 Button 개발
- ✅ 실무에서 바로 사용할 수 있는 패턴

---

## 기본 Button 이해하기

### Compose의 기본 Button 종류

Jetpack Compose는 Material Design 3 기반의 다양한 Button을 제공합니다:

```kotlin
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun BasicButtonsExample() {
    Column {
        // 1. Filled Button (기본)
        Button(onClick = { /* 처리 */ }) {
            Text("Filled Button")
        }
        
        // 2. Outlined Button
        OutlinedButton(onClick = { /* 처리 */ }) {
            Text("Outlined Button")
        }
        
        // 3. Text Button
        TextButton(onClick = { /* 처리 */ }) {
            Text("Text Button")
        }
        
        // 4. Elevated Button
        ElevatedButton(onClick = { /* 처리 */ }) {
            Text("Elevated Button")
        }
        
        // 5. Filled Tonal Button
        FilledTonalButton(onClick = { /* 처리 */ }) {
            Text("Filled Tonal Button")
        }
    }
}
```

**실제 UI 예시:**

![Material Design 3 버튼 타입](./images/material3_button_types.png)

### Button의 기본 구조

```kotlin
@Composable
fun ButtonStructure() {
    Button(
        onClick = { /* 클릭 이벤트 */ },              // 필수: 클릭 핸들러
        modifier = Modifier,                            // 선택: 크기, 패딩 등
        enabled = true,                                 // 선택: 활성화 여부
        shape = ButtonDefaults.shape,                   // 선택: 모양
        colors = ButtonDefaults.buttonColors(),         // 선택: 색상
        elevation = ButtonDefaults.buttonElevation(),   // 선택: 그림자
        border = null,                                  // 선택: 테두리
        contentPadding = ButtonDefaults.ContentPadding, // 선택: 내부 패딩
        interactionSource = remember { MutableInteractionSource() }
    ) {
        // 버튼 내용 (Text, Icon 등)
        Text("Click Me")
    }
}
```

---

## 실무 Button 설계 원칙

### 1. 디자인 시스템 기반 설계

실무에서는 디자인 시스템을 기반으로 일관된 Button을 만들어야 합니다.

```kotlin
// 디자인 시스템의 Button 스타일 정의
enum class ButtonSize {
    SMALL,   // 높이 32dp
    MEDIUM,  // 높이 40dp (기본)
    LARGE    // 높이 48dp
}

enum class ButtonVariant {
    PRIMARY,     // 주요 액션
    SECONDARY,   // 보조 액션
    TERTIARY,    // 최소한의 강조
    DANGER,      // 위험한 액션
    SUCCESS,     // 성공/확인
    DISABLED     // 비활성화
}
```

### 2. 단일 책임 원칙

Button 컴포넌트는 **하나의 명확한 역할**만 수행해야 합니다:

```kotlin
// ❌ 나쁜 예: Button이 너무 많은 책임을 가짐
@Composable
fun BadButton(
    text: String,
    onClick: () -> Unit,
    isLoading: Boolean,
    showIcon: Boolean,
    iconRes: Int?,
    validateBeforeClick: () -> Boolean,
    onValidationFailed: () -> Unit
) { /* ... */ }

// ✅ 좋은 예: 명확한 책임 분리
@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: ButtonSize = ButtonSize.MEDIUM,
    variant: ButtonVariant = ButtonVariant.PRIMARY,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null
) { /* ... */ }
```

### 3. 확장 가능성

나중에 새로운 요구사항이 추가되어도 쉽게 확장할 수 있어야 합니다.

---

## 재사용 가능한 Button 구현

### 기본 Button 컴포넌트

```kotlin
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 실무용 재사용 가능한 Button 컴포넌트
 * 
 * @param text 버튼에 표시할 텍스트
 * @param onClick 클릭 이벤트 핸들러
 * @param modifier Modifier
 * @param size 버튼 크기 (SMALL, MEDIUM, LARGE)
 * @param variant 버튼 스타일 (PRIMARY, SECONDARY 등)
 * @param enabled 활성화 여부
 * @param leadingIcon 텍스트 앞에 표시할 아이콘 (선택)
 * @param trailingIcon 텍스트 뒤에 표시할 아이콘 (선택)
 * @param fullWidth 전체 너비 사용 여부
 */
@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: ButtonSize = ButtonSize.MEDIUM,
    variant: ButtonVariant = ButtonVariant.PRIMARY,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    fullWidth: Boolean = false
) {
    // 크기에 따른 높이 설정
    val height = when (size) {
        ButtonSize.SMALL -> 32.dp
        ButtonSize.MEDIUM -> 40.dp
        ButtonSize.LARGE -> 48.dp
    }
    
    // 크기에 따른 텍스트 크기 설정
    val fontSize = when (size) {
        ButtonSize.SMALL -> 12.sp
        ButtonSize.MEDIUM -> 14.sp
        ButtonSize.LARGE -> 16.sp
    }
    
    // 크기에 따른 패딩 설정
    val horizontalPadding = when (size) {
        ButtonSize.SMALL -> 12.dp
        ButtonSize.MEDIUM -> 16.dp
        ButtonSize.LARGE -> 20.dp
    }
    
    // 스타일에 따른 색상 설정
    val colors = getButtonColors(variant)
    
    // 기본 Modifier + 사용자 Modifier
    val buttonModifier = modifier
        .height(height)
        .then(
            if (fullWidth) Modifier.fillMaxWidth()
            else Modifier
        )
    
    Button(
        onClick = onClick,
        modifier = buttonModifier,
        enabled = enabled && variant != ButtonVariant.DISABLED,
        colors = colors,
        contentPadding = PaddingValues(
            horizontal = horizontalPadding,
            vertical = 8.dp
        ),
        shape = MaterialTheme.shapes.medium // 또는 커스텀 shape
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Leading Icon
            leadingIcon?.let {
                it()
                Spacer(modifier = Modifier.width(8.dp))
            }
            
            // Text
            Text(
                text = text,
                fontSize = fontSize,
                fontWeight = FontWeight.Medium
            )
            
            // Trailing Icon
            trailingIcon?.let {
                Spacer(modifier = Modifier.width(8.dp))
                it()
            }
        }
    }
}

/**
 * 버튼 스타일에 따른 색상 반환
 */
@Composable
private fun getButtonColors(variant: ButtonVariant): ButtonColors {
    return when (variant) {
        ButtonVariant.PRIMARY -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
        ButtonVariant.SECONDARY -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary
        )
        ButtonVariant.TERTIARY -> ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.primary
        )
        ButtonVariant.DANGER -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError
        )
        ButtonVariant.SUCCESS -> ButtonDefaults.buttonColors(
            containerColor = Color(0xFF4CAF50), // 녹색
            contentColor = Color.White
        )
        ButtonVariant.DISABLED -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
```

### 사용 예제

```kotlin
@Composable
fun ButtonUsageExample() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 기본 사용
        AppButton(
            text = "저장",
            onClick = { /* 저장 로직 */ }
        )
        
        // 크기 변경
        AppButton(
            text = "작은 버튼",
            onClick = { },
            size = ButtonSize.SMALL
        )
        
        // 스타일 변경
        AppButton(
            text = "삭제",
            onClick = { },
            variant = ButtonVariant.DANGER
        )
        
        // 아이콘 포함
        AppButton(
            text = "업로드",
            onClick = { },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Upload,
                    contentDescription = null
                )
            }
        )
        
        // 전체 너비
        AppButton(
            text = "로그인",
            onClick = { },
            fullWidth = true,
            size = ButtonSize.LARGE
        )
        
        // 비활성화
        AppButton(
            text = "제출",
            onClick = { },
            enabled = false
        )
    }
}
```

---

## 고급 패턴

### 1. Loading 상태가 있는 Button

```kotlin
@Composable
fun LoadingButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    size: ButtonSize = ButtonSize.MEDIUM,
    variant: ButtonVariant = ButtonVariant.PRIMARY
) {
    AppButton(
        text = if (isLoading) "" else text,
        onClick = onClick,
        modifier = modifier,
        size = size,
        variant = variant,
        enabled = enabled && !isLoading,
        leadingIcon = if (isLoading) {
            {
                CircularProgressIndicator(
        onClick = { showDialog = true },
        modifier = modifier,
        variant = variant
    )
    
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("확인") },
            text = { Text(confirmMessage) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onConfirm()
                        showDialog = false
                    }
                ) {
                    Text("확인")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("취소")
                }
            }
        )
    }
}

// 사용 예
@Composable
fun ConfirmButtonExample() {
    ConfirmButton(
        text = "계정 삭제",
        confirmMessage = "정말로 계정을 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.",
        onConfirm = {
            // 계정 삭제 로직
        }
    )
}
```

### 3. 아이콘 전용 Button

```kotlin
@Composable
fun IconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: ButtonSize = ButtonSize.MEDIUM,
    variant: ButtonVariant = ButtonVariant.PRIMARY,
    enabled: Boolean = true
) {
    val iconSize = when (size) {
        ButtonSize.SMALL -> 16.dp
        ButtonSize.MEDIUM -> 20.dp
        ButtonSize.LARGE -> 24.dp
    }
    
    val buttonSize = when (size) {
        ButtonSize.SMALL -> 32.dp
        ButtonSize.MEDIUM -> 40.dp
        ButtonSize.LARGE -> 48.dp
    }
    
    val colors = getButtonColors(variant)
    
    Button(
        onClick = onClick,
        modifier = modifier.size(buttonSize),
        enabled = enabled,
        colors = colors,
        contentPadding = PaddingValues(0.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(iconSize)
        )
    }
}
```

### 4. 그룹화된 Button (Segmented Control)

```kotlin
@Composable
fun <T> SegmentedButton(
    options: List<T>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    optionLabel: (T) -> String = { it.toString() }
) {
    Row(
        modifier = modifier
            .height(40.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = MaterialTheme.shapes.medium
            )
            .clip(MaterialTheme.shapes.medium)
    ) {
        options.forEachIndexed { index, option ->
            val isSelected = option == selectedOption
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else Color.Transparent
                    )
                    .clickable { onOptionSelected(option) }
                    .then(
                        if (index < options.size - 1) {
                            Modifier.border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = RectangleShape
                            )
                        } else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = optionLabel(option),
                    color = if (isSelected) 
                        MaterialTheme.colorScheme.onPrimary
                    else 
                        MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                )
            }
        }
    }
}

// 사용 예
@Composable
fun SegmentedButtonExample() {
    enum class ViewMode { LIST, GRID, CALENDAR }
    
    var selectedMode by remember { mutableStateOf(ViewMode.LIST) }
    
    SegmentedButton(
        options = ViewMode.values().toList(),
        selectedOption = selectedMode,
        onOptionSelected = { selectedMode = it },
        optionLabel = { it.name }
    )
}
```

---

## 접근성과 모범 사례

### 1. 접근성 고려사항

```kotlin
@Composable
fun AccessibleButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null // 스크린 리더를 위한 설명
) {
    AppButton(
        text = text,
        onClick = onClick,
        modifier = modifier.semantics {
            // 추가 접근성 정보 제공
            contentDescription?.let {
                this.contentDescription = it
            }
            
            // 버튼 역할 명시
            role = Role.Button
        }
    )
}
```

### 2. 터치 타겟 크기

Material Design 가이드라인에 따르면 최소 터치 타겟은 **48x48dp**입니다:

```kotlin
@Composable
fun AccessibleSizeButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppButton(
        text = text,
        onClick = onClick,
        modifier = modifier
            .defaultMinSize(minWidth = 64.dp, minHeight = 48.dp)
    )
}
```

### 3. 색상 대비

WCAG 가이드라인을 따라 충분한 색상 대비를 유지해야 합니다:

```kotlin
// 커스텀 테마에서 색상 대비 확인
@Composable
fun getAccessibleButtonColors(): ButtonColors {
    return ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        // 대비율이 최소 4.5:1 이상인지 확인
    )
}
```

### 4. 로딩 상태 접근성

```kotlin
@Composable
fun AccessibleLoadingButton(
    text: String,
    onClick: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    LoadingButton(
        text = text,
        onClick = onClick,
        isLoading = isLoading,
        modifier = modifier.semantics {
            // 로딩 상태를 스크린 리더에 알림
            stateDescription = if (isLoading) "로딩 중" else "준비됨"
        }
    )
}
```

---

## 실전 예제

### 예제 1: 로그인 화면 Button

```kotlin
@Composable
fun LoginScreen() {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("이메일") },
            modifier = Modifier.fillMaxWidth()
        )
        
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("비밀번호") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 로그인 버튼
        LoadingButton(
            text = "로그인",
            onClick = {
                isLoading = true
                // 로그인 로직
            },
            isLoading = isLoading,
            fullWidth = true,
            size = ButtonSize.LARGE,
            variant = ButtonVariant.PRIMARY
        )
        
        // 소셜 로그인 버튼들
        AppButton(
            text = "Google로 계속하기",
            onClick = { /* Google 로그인 */ },
            fullWidth = true,
            variant = ButtonVariant.SECONDARY,
            leadingIcon = {
                // Google 아이콘
            }
        )
        
        // 회원가입 버튼
        TextButton(
            onClick = { /* 회원가입 화면으로 이동 */ },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("계정이 없으신가요? 회원가입")
        }
    }
}
```

### 예제 2: 폼 제출 Button

```kotlin
@Composable
fun FormSubmitExample() {
    var formData by remember { mutableStateOf(FormData()) }
    var isSubmitting by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    
    Column(modifier = Modifier.padding(16.dp)) {
        // 폼 필드들...
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 취소 버튼
            AppButton(
                text = "취소",
                onClick = { /* 취소 로직 */ },
                variant = ButtonVariant.SECONDARY,
                modifier = Modifier.weight(1f)
            )
            
            // 제출 버튼
            LoadingButton(
                text = "제출",
                onClick = {
                    if (validateForm(formData)) {
                        isSubmitting = true
                        submitForm(formData)
                    } else {
                        showError = true
                    }
                },
                isLoading = isSubmitting,
                enabled = !isSubmitting,
                variant = ButtonVariant.PRIMARY,
                modifier = Modifier.weight(1f)
            )
        }
        
        if (showError) {
            Text(
                text = "모든 필드를 올바르게 입력해주세요.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
```

### 예제 3: 액션 시트 Button

```kotlin
@Composable
fun ActionSheetExample(
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "작업 선택",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // 수정 버튼
            AppButton(
                text = "수정",
                onClick = { /* 수정 로직 */ },
                variant = ButtonVariant.PRIMARY,
                fullWidth = true,
                leadingIcon = {
                    Icon(Icons.Default.Edit, contentDescription = null)
                }
            )
            
            // 공유 버튼
            AppButton(
                text = "공유",
                onClick = { /* 공유 로직 */ },
                variant = ButtonVariant.SECONDARY,
                fullWidth = true,
                leadingIcon = {
                    Icon(Icons.Default.Share, contentDescription = null)
                }
            )
            
            // 삭제 버튼 (확인 다이얼로그 포함)
            ConfirmButton(
                text = "삭제",
                confirmMessage = "정말로 삭제하시겠습니까?",
                onConfirm = {
                    onDelete()
                    onDismiss()
                },
                variant = ButtonVariant.DANGER,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
```

### 예제 4: FAB (Floating Action Button)

```kotlin
@Composable
fun FabExample(
    onCreateNew: () -> Unit
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateNew,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "새로 만들기"
                )
            }
        }
    ) { paddingValues ->
        // 메인 콘텐츠
    }
}
```

---

## 요약 및 체크리스트

### ✅ 실무용 Button 설계 체크리스트

- [ ] **일관성**: 디자인 시스템에 맞는 스타일 정의
- [ ] **재사용성**: 다양한 상황에서 사용 가능한 API 설계
- [ ] **확장성**: 새로운 요구사항에 쉽게 대응 가능
- [ ] **접근성**: 
  - [ ] 최소 터치 타겟 크기 (48x48dp)
  - [ ] 충분한 색상 대비
  - [ ] 스크린 리더 지원
  - [ ] 키보드 네비게이션 지원
- [ ] **상태 관리**:
  - [ ] enabled/disabled
  - [ ] loading
  - [ ] focus
  - [ ] pressed
- [ ] **에러 처리**: 사용자에게 명확한 피드백 제공
- [ ] **성능**: 불필요한 리컴포지션 방지

### 핵심 원칙

1. **Keep It Simple**: 복잡하게 만들지 말고 명확하게
2. **Consistent**: 앱 전체에서 일관된 사용
3. **Accessible**: 모든 사용자가 사용할 수 있도록
4. **Testable**: 테스트하기 쉬운 구조

---

## 참고 자료

- [Material Design 3 - Buttons](https://m3.material.io/components/buttons)
- [Jetpack Compose Button Documentation](https://developer.android.com/jetpack/compose/components/button)
- [Accessibility in Compose](https://developer.android.com/jetpack/compose/accessibility)
- [Design Systems for Developers](https://www.learnstorybook.com/design-systems-for-developers/)

---

**다음 단계**: 
- [06. 테마 & 스타일링](./06-jetpack-compose-theming-guide.md)에서 디자인 시스템 구축 방법을 학습하세요.
- [36. Material Design 3 고급](./36-material-design-3-advanced.md)에서 더 고급 컴포넌트를 살펴보세요.

Happy Composing! 🚀
