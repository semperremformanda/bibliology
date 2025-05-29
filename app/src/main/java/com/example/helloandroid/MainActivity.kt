package com.webook.app


import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.helloandroid.ui.theme.HelloAndroidTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import android.app.DatePickerDialog
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import java.util.*
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.helloandroid.network.BookInfo
import com.example.helloandroid.network.Coupon
import com.example.helloandroid.viewmodel.UserViewModel
import com.example.helloandroid.network.UserInfo
import java.time.LocalDate
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Star
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPref = getSharedPreferences("session", Context.MODE_PRIVATE)
        val savedUserId = sharedPref.getString("userId", null)
        setContent {
            val vm: vm = viewModel()
            val userViewModel: UserViewModel = viewModel()

            LaunchedEffect(Unit) {
                if (savedUserId != null) {
                    userViewModel.infofind(savedUserId) { userInfo ->
                        if (userInfo != null) {
                            userViewModel.loggedInUser = userInfo
                            vm.ishere = true
                        }
                    }
                }
            }
            HelloAndroidTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.rbimage), // 배경 이미지 리소스
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                    Surface(
                        color = Color.Transparent
                    ) {
                        LoginApp(vm,userViewModel)
                    }
                }
            }
        }

    }
}

@Suppress("ClassName")
class vm: ViewModel() {
    var Screen by mutableStateOf("home")
    fun navigate(screen: String) { Screen = screen }

    var book by  mutableStateOf<BookInfo?>(null)
    var ishere by mutableStateOf(false)
}



@Composable
fun MainScreenWithBottomNav(vm: vm, userViewModel: UserViewModel) {
    userViewModel.fetchAllBooks()
    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = vm.Screen == "advanced",
                    onClick = { vm.navigate("advanced") },
                    icon = { Icon(Icons.Default.Search, contentDescription = null) },
                    label = { Text("고급검색") }
                )
                NavigationBarItem(
                    selected = vm.Screen == "home",
                    onClick = { vm.navigate("home") },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("홈") }
                )
                if (!vm.ishere) {
                    NavigationBarItem(
                        selected = false,
                        onClick = { vm.navigate("login") },
                        icon = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
                        label = { Text("로그인하기") }
                    )
                } else {
                    NavigationBarItem(
                        selected = vm.Screen in listOf("coupon", "manage", "cart", "purchased", "reply", "favorite"),
                        onClick = { vm.navigate("manage") },
                        icon = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
                        label = { Text("회원정보") }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (vm.Screen) {
                "home" -> HomeScreen(vm,userViewModel)
                "advanced" -> AdvancedScreen(vm,userViewModel)
                "manage" -> ManageScreen(vm,userViewModel)
                "coupon" -> CouponScreen(userViewModel)
                "purchased" -> PurchasedScreen(userViewModel)
                "cart" -> CartScreen(vm,userViewModel)
                "reply" -> ReplyScreen(vm,userViewModel)
                "favorite" -> FavoriteScreen(vm,userViewModel)
            }
        }
    }
}
fun clearUserId(context: Context) {
    val sharedPref = context.getSharedPreferences("session", Context.MODE_PRIVATE)
    sharedPref.edit().remove("userId").apply()
}



@Composable
fun LoginApp(vm:vm,userViewModel: UserViewModel) {
    val activity = LocalContext.current as? Activity
    var showExitDialog by remember { mutableStateOf(false) }
    BackHandler(enabled = true) {
        when (vm.Screen) {
            "home" -> { showExitDialog = true} // 메인탭이면 아무 동작 안함
            "coupon","cart","purchased","reply","favorite","editInfo"->vm.navigate("manage")
            else -> vm.navigate("home")
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("앱 종료") },
            text = { Text("앱을 종료하시겠습니까?") },
            confirmButton = {
                TextButton(onClick = {
                    showExitDialog = false
                    activity?.finish()
                }) {
                    Text("확인")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("취소")
                }
            }
        )
    }


    if (vm.Screen in listOf("home", "advanced", "manage","coupon","cart","purchased","reply","favorite")) {
        MainScreenWithBottomNav(vm,userViewModel)
    } else {
        when (vm.Screen) {
            "login" -> LoginScreen(vm,userViewModel)
            "register" -> RegisterScreen(vm,userViewModel)
            "detail" -> DetailScreen(vm,userViewModel)
            "bill" -> BillScreen(vm,userViewModel)
            "reset" -> ResetScreen(vm,userViewModel)
            "editInfo" -> EditInfoScreen(vm,userViewModel)
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(vm: vm, userViewModel: UserViewModel) {
    var userId by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var errorMessage by rememberSaveable { mutableStateOf("") }
    var isLoading by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current


    val primaryColor = Color(0xFFFFC107) // 노란색 계열
    val buttonColor = Color(0xFF1A237E)  // 딥블루
    val errorColor = Color(0xFFD32F2F)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "\"사람은 책을 만들고",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = buttonColor
            ),
        )
        Text(
            text = " 책은 사람을 만든다.\"",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = buttonColor
            ),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = userId,
            onValueChange = { userId = it },
            label = { Text("아이디") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, shape = RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = primaryColor,
                unfocusedBorderColor = Color.LightGray,
                containerColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 🔸 PW
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("비밀번호") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, shape = RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = primaryColor,
                unfocusedBorderColor = Color.LightGray,
                containerColor = Color.White
            )
        )

        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage,
                color = errorColor,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.Start)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 🔸 로그인 버튼
        Button(
            onClick = {
                isLoading = true
                userViewModel.login(userId, password) { success, error ->
                    isLoading = false
                    if (success) {
                        userViewModel.infofind(userId) { userinfo ->
                            userViewModel.loggedInUser = userinfo
                        }
                        vm.ishere = true
                       // saveUserId(context, userId)
                        context.getSharedPreferences("session", Context.MODE_PRIVATE).edit()
                            .putString("userId", userId).apply()
                        errorMessage = ""
                        vm.navigate("home")
                    } else {
                        errorMessage = error
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text("로그인", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 🔸 비밀번호 재설정
        TextButton(
            onClick = { vm.navigate("reset") },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(
                "비밀번호를 잊으셨나요?",
                color = Color.Gray,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}






@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetScreen(vm: vm, userViewModel: UserViewModel) {
    var emailId by rememberSaveable { mutableStateOf("") }
    var selectedDomain by rememberSaveable { mutableStateOf("naver.com") }
    val domainOptions = listOf("naver.com", "gmail.com", "hansung.ac.kr")

    var verificationCode by rememberSaveable { mutableStateOf("") }
    var verificationSent by rememberSaveable { mutableStateOf(false) }
    var verificationPassed by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf("") }
    var loading by rememberSaveable { mutableStateOf(false) }

    val fullEmail = "$emailId@$selectedDomain"
    val primaryColor = MaterialTheme.colorScheme.primary
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "비밀번호 찾기",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = primaryColor
            ),
            modifier = Modifier.padding(bottom = 32.dp)
        )

        if (!verificationSent) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {



                OutlinedTextField(
                    value = emailId,
                    onValueChange = { emailId = it },
                    label = { Text("이메일") },
                    singleLine = true,
                    modifier = Modifier
                        .weight(0.8f)
                        .padding(4.dp)
                        .shadow(2.dp, shape = RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = Color.LightGray,
                        containerColor = Color.White
                    )
                )

                Text("@", modifier = Modifier.padding(horizontal = 4.dp), color = primaryColor)


                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier
                        .weight(1.2f)
                        .padding(4.dp)
                ) {
                    OutlinedTextField(
                        value = selectedDomain,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("도메인 선택") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .shadow(2.dp, shape = RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = Color.LightGray,
                            containerColor = Color.White
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        domainOptions.forEach { domain ->
                            DropdownMenuItem(
                                text = { Text(domain) },
                                onClick = {
                                    selectedDomain = domain
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            Button(
                onClick = {
                    if (emailId.isNotBlank()) {
                        loading = true
                        userViewModel.sendVerificationCode(fullEmail) { success, error ->
                            loading = false
                            if (success) {
                                verificationSent = true
                                errorMessage = ""
                            } else {
                                errorMessage = error ?: "이메일 전송 실패"
                            }
                        }
                    } else {
                        errorMessage = "이메일 아이디를 입력하세요."
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Green,
                    contentColor = Color.Black
                ),
                enabled = !loading
            ) {
                if (loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("인증 코드 받기")
                }
            }

            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(errorMessage, color = MaterialTheme.colorScheme.error)
            }

        } else if (!verificationPassed) {
            OutlinedTextField(
                value = verificationCode,
                onValueChange = { verificationCode = it },
                label = { Text("인증 코드 입력") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .shadow(2.dp, shape = RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = primaryColor,
                    unfocusedBorderColor = Color.LightGray,
                    containerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (verificationCode.isNotEmpty()) {
                        loading = true
                        userViewModel.verifyCode(fullEmail, verificationCode) { success, password ->
                            loading = false
                            if (success) {
                                verificationPassed = true
                                errorMessage = ""
                            } else {
                                errorMessage = password ?: "인증 코드가 틀렸습니다."
                            }
                        }
                    } else {
                        errorMessage = "인증 코드를 입력하세요."
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    contentColor = Color.White
                )
            ) {
                Text("확인")
            }

            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(errorMessage, color = MaterialTheme.colorScheme.error)
            }

        } else {
            Text(
                text = "비밀번호가 이메일로 전송되었습니다!",
                style = MaterialTheme.typography.bodyLarge.copy(color = primaryColor)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { vm.navigate("login") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    contentColor = Color.White
                )
            ) {
                Text("로그인 화면으로")
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(vm:vm,userViewModel: UserViewModel) {
    val context = LocalContext.current
    val user = userViewModel.loggedInUser
    val bookList = userViewModel.bookList.value
    val searchHistory = userViewModel.searchHistory.value

    var searchText by rememberSaveable { mutableStateOf("") }
    var searchType by rememberSaveable { mutableStateOf("제목") }
    var dropdownExpanded by rememberSaveable { mutableStateOf(false) }
    var showmore by rememberSaveable { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    var isTextFieldFocused by rememberSaveable { mutableStateOf(false) }

    val genreOptions = listOf("전체", "풍자", "디스토피아", "고전", "전후소설", "동화")
    var selectedGenre by rememberSaveable { mutableStateOf("전체") }

    var selectedSort by rememberSaveable { mutableStateOf("댓글순") }
    var sortDropdownExpanded by rememberSaveable { mutableStateOf(false) }

    var selectedOrder by rememberSaveable { mutableStateOf(true) } // true = 내림차순

    LaunchedEffect(user?.tid) {
        user?.tid?.let { userViewModel.fetchUserInterests(it) }
    }

    val filteredBooks by remember(searchText, selectedGenre, selectedSort, selectedOrder, bookList) {
        derivedStateOf {
            val filteredBySearch = when {
                searchText.isBlank() -> bookList
                searchType == "제목" -> bookList.filter { it.title.contains(searchText, ignoreCase = true) }
                searchType == "작가" -> bookList.filter { it.author.contains(searchText, ignoreCase = true) }
                else -> bookList.filter { it.publisher.contains(searchText, ignoreCase = true) }
            }

            val filteredByGenre = if (selectedGenre == "전체") bookList
            else bookList.filter { it.genre.contains(selectedGenre) }

            val intersected = filteredBySearch.intersect(filteredByGenre.toSet()).toList()

            val sorted = when (selectedSort) {
                "댓글순" -> intersected.sortedBy { it.count }
                "가나다순" -> intersected.sortedBy { it.title }
                else -> intersected.sortedBy { it.score }
            }

            if (selectedOrder) sorted.reversed() else sorted
        }
    }
    fun getAutoCompleteSuggestions(input: String): List<String> {
        if (input.isBlank()) return emptyList()
        val fieldSelector: (BookInfo) -> String = when (searchType) {
            "제목" -> { it -> it.title }
            "작가" -> { it -> it.author }
            else   -> { it -> it.publisher }
        }
        return bookList
            .map(fieldSelector)
            .filter { it.startsWith(input, ignoreCase = true) }
    }
    fun getFilteredSearchHistory(): List<String> {
        return when (searchType) {
            "제목" -> searchHistory.mapNotNull { it.title }
            "작가" -> searchHistory.mapNotNull { it.author }
            "출판사" -> searchHistory.mapNotNull { it.publisher }
            else -> emptyList()
        }
    }



    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding()
    ) {
        item {
            TopAppBar(
                title = {
                    val clickme = if (showmore) "📖 WEbook" else "📚 WEbook"

                    TextButton(
                        onClick = { showmore = !showmore }
                    ) {
                        Text(
                            text = clickme,
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                },
                actions = {
                    if (!vm.ishere) {
                        TextButton(onClick = { vm.navigate("login") }) { Text("로그인", color = Color.White) }
                        TextButton(onClick = { vm.navigate("register") }) { Text("회원가입", color = Color.White) }
                    } else {
                        TextButton(onClick = { clearUserId(context);vm.ishere=false }) { Text("로그아웃", color = Color.White) }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1A73E8))
            )
        }  //Topbar

        item {
            AnimatedVisibility(
                visible = showmore,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8)),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("정렬:", style = MaterialTheme.typography.bodyMedium)
                            TextButton(onClick = { sortDropdownExpanded = true }) {
                                Text(selectedSort)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                            DropdownMenu(
                                expanded = sortDropdownExpanded,
                                onDismissRequest = { sortDropdownExpanded = false }
                            ) {
                                listOf("댓글순", "가나다순", "별점순").forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            selectedSort = option
                                            sortDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        IconButton(onClick = {
                            selectedOrder = !selectedOrder
                        }) {
                            Icon(
                                imageVector = if (selectedOrder)
                                    Icons.Default.KeyboardArrowDown
                                else Icons.Default.KeyboardArrowUp,
                                contentDescription = null,
                                tint = Color(0xFF1A73E8)
                            )
                        }
                    }


                    HorizontalDivider(thickness = 2.dp, color = Color(0xFF4472C4))
                    Spacer(modifier = Modifier.height(12.dp))


                    genreOptions.chunked(3).forEach { rowOptions ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowOptions.forEach { option ->
                                val isSelected = selectedGenre == option
                                val backgroundColor = if (isSelected) Color(0xFF4DD0E1) else Color.White
                                val textColor = if (isSelected) Color.White else Color.Black
                                val borderColor = if (isSelected) Color(0xFF4DD0E1) else Color.LightGray

                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(20.dp))
                                        .clickable {
                                            selectedGenre = option
                                        },
                                    shape = RoundedCornerShape(20.dp),
                                    color = backgroundColor,
                                    border = BorderStroke(1.dp, borderColor)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                            .fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "선택됨",
                                                tint = Color.White,
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .padding(end = 4.dp)
                                            )
                                        }
                                        Text(text = option, color = textColor)
                                    }
                                }
                            }

                            repeat(3 - rowOptions.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
            }
        }  //세부사항

        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start=16.dp, end=16.dp,top=16.dp)
            ) {
                TextField(
                    value = searchText,
                    onValueChange = { searchText = it.trim() },
                    label = { Text("검색어 입력") },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "검색",
                            modifier = Modifier.clickable {
                                focusManager.clearFocus(force = true)
                                if(searchText!="") {
                                    userViewModel.searchBooks(
                                        userId = user?.tid,
                                        title = if (searchType == "제목") searchText else null,
                                        author = if (searchType == "작가") searchText else null,
                                        publisher = if (searchType == "출판사") searchText else null,
                                        genre = null
                                    )
                                }
                            }
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { focusState ->
                            isTextFieldFocused = focusState.isFocused
                        }
                    ,
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            if(searchText!="") {
                                userViewModel.searchBooks(
                                    userId = user?.tid,
                                    title = if (searchType == "제목") searchText else null,
                                    author = if (searchType == "작가") searchText else null,
                                    publisher = if (searchType == "출판사") searchText else null,
                                    genre = null
                                )
                            }
                            isTextFieldFocused = false
                            focusManager.clearFocus(force = true)
                        }
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded= !dropdownExpanded }
                ) {
                    OutlinedButton(
                        onClick = { dropdownExpanded = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .menuAnchor()
                            .width(90.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(red = 91, green = 134, blue = 87)
                        )
                    ) {
                        Text(text = searchType)
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "드롭다운"
                        )
                    }

                    ExposedDropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        listOf("제목", "작가", "출판사").forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    searchType = option
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }  //검색창

        item {
            val autoCompleteList = getAutoCompleteSuggestions(searchText)
            val searchHistoryList = getFilteredSearchHistory()

            if (isTextFieldFocused && (autoCompleteList.isNotEmpty() || (searchHistoryList.isNotEmpty() && vm.ishere))) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 110.dp),
                    elevation = CardDefaults.cardElevation(6.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {

                        if (autoCompleteList.isNotEmpty()) {
                            Text(
                                "추천 검색",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color(0xFF1A73E8),
                                    fontWeight = FontWeight.SemiBold
                                ),
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            autoCompleteList.forEach { suggestion ->
                                Text(
                                    text = suggestion,
                                    color = Color.Black,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            searchText = suggestion
                                            isTextFieldFocused = false
                                            focusManager.clearFocus(force = true)
                                            userViewModel.searchBooks(
                                                userId = user?.tid,
                                                title = if (searchType == "제목") searchText else null,
                                                author = if (searchType == "작가") searchText else null,
                                                publisher = if (searchType == "출판사") searchText else null,
                                                genre = null
                                            )
                                        }
                                        .padding(vertical = 6.dp, horizontal = 8.dp)
                                )
                            }

                            if (searchHistoryList.isNotEmpty()) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp), thickness = 2.dp,
                                    color = Color.LightGray
                                )
                            }
                        }

                        if (searchHistoryList.isNotEmpty() && vm.ishere) {
                            searchHistoryList.forEach { history ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            searchText = history
                                            isTextFieldFocused = false
                                            focusManager.clearFocus(force = true)
                                                userViewModel.searchBooks(
                                                    userId = user?.tid,
                                                    title = if (searchType == "제목") searchText else null,
                                                    author = if (searchType == "작가") searchText else null,
                                                    publisher = if (searchType == "출판사") searchText else null,
                                                    genre = null
                                                )
                                        }
                                        .padding(vertical = 2.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.baseline_access_time_24),
                                        contentDescription = "히스토리 아이콘",
                                        modifier = Modifier
                                            .size(30.dp)
                                            .padding(end = 6.dp),
                                        colorFilter = ColorFilter.tint(Color.Gray)  // 색상 지정 (optional)
                                    )
                                    Text(
                                        history,
                                        color = Color.DarkGray,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(onClick = {
                                        userViewModel.deleteSearchHistoryByKeyword(user!!.tid!!, history) {
                                            userViewModel.fetchUserSearchHistory(user.tid!!)
                                        }
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "삭제",
                                            tint = Color.Gray
                                        )
                                    }
                                }
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(end = 4.dp), // 약간의 여유 padding
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = {
                                        userViewModel.deleteAllSearchHistory(user!!.tid!!) {
                                            userViewModel.fetchUserSearchHistory(user.tid!!)
                                        }
                                    }
                                ) {
                                    Text("전체 삭제", color = Color.Red)
                                }
                            }
                        }
                    }
                }
            }
        }

        items(filteredBooks.chunked(2),) { bookPair ->
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (book in bookPair) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        BookCard(book = book, userViewModel = userViewModel,vm)
                    }
                }

                if (bookPair.size < 2) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}


@Composable
fun BookCard(book: BookInfo, userViewModel: UserViewModel,vm:vm) {
    var bookScore by remember { mutableDoubleStateOf(-0.1) }

    val userInterests = userViewModel.userInterests.value
    val isHighlighted = userInterests.contains(book.genre)&&vm.ishere
    userViewModel.fetchAverageRating(book.id) { score -> bookScore = score }
    fun renderStars(score: Double): @Composable () -> Unit = {
        val filledStars = score.toInt()
        val emptyStars = 5 - filledStars
        Row {
            repeat(filledStars) {
                Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
            }
            repeat(emptyStars) {
                Icon(Icons.Outlined.Star, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
            }
        }
    }

    // 카드 배경색 (책 느낌 연한 베이지, 강조 시 더 밝은 톤)
    val backgroundColor = if (isHighlighted) Color(0xFFFFEEDB) else Color(0xFFF9F4EC)


    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        border = if (isHighlighted) BorderStroke(2.dp, Color(0xFFE67E22)) else BorderStroke(2.dp, Color(0xFF4472C4)),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                vm.book = book
                vm.navigate("detail")
            }
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isHighlighted) {
                Text(
                    text = "🎯 관심 장르!",
                    color = Color(0xFFE67E22),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = 4.dp)
                        .background(Color(0xFFFFE0B2), shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
                Image(
                    painter = rememberAsyncImagePainter("http://23.21.136.176:8080/${book.imageUrl}"),
                    contentDescription = "${book.title} 이미지",
                    modifier = Modifier
                        .size(if (isHighlighted) 140.dp else 164.dp)
                        .padding(8.dp)
                )




            Text(
                text = book.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(bottom = 4.dp)
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
            )

            if (bookScore != -0.1) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    renderStars(bookScore)()
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "(${String.format("%.1f", bookScore)})",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            } else {
                Text("(별점 로딩 중...)", style = MaterialTheme.typography.bodySmall)
            }

            Text(
                text = "저자: ${book.author}",
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
            )

            Text(
                text = "${book.price}₩",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}





@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedScreen(vm: vm, userViewModel: UserViewModel) {
    val bookList = userViewModel.bookList.value

    var authorText by rememberSaveable { mutableStateOf("") }
    var titleText by rememberSaveable { mutableStateOf("") }
    var companyText by rememberSaveable { mutableStateOf("") }

    val genreOptions = listOf("전체", "풍자", "디스토피아", "고전", "전후소설", "동화")
    var selectedGenre by rememberSaveable { mutableStateOf("전체") }

    val advancedFilteredBooks by remember(titleText, authorText, companyText, selectedGenre, bookList) {
        derivedStateOf {
            bookList.filter { book ->
                val matchAuthor = authorText.isBlank() || book.author.contains(authorText, ignoreCase = true)
                val matchTitle = titleText.isBlank() || book.title.contains(titleText, ignoreCase = true)
                val matchCompany = companyText.isBlank() || book.publisher.contains(companyText, ignoreCase = true)
                val matchGenre = (selectedGenre == "전체") || book.genre.contains(selectedGenre)
                matchAuthor && matchTitle && matchGenre && matchCompany
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(0.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(6.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "고급 검색",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF3563E9)),
                        modifier = Modifier.padding(bottom = 18.dp)
                    )

                    OutlinedTextField(
                        value = titleText,
                        onValueChange = { titleText = it },
                        label = { Text("제목 입력") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color(0xFF3563E9),
                            unfocusedBorderColor = Color.LightGray,
                            containerColor = Color(0xFFF6F8FB)
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = authorText,
                        onValueChange = { authorText = it },
                        label = { Text("작가 입력") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color(0xFF3563E9),
                            unfocusedBorderColor = Color.LightGray,
                            containerColor = Color(0xFFF6F8FB)
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = companyText,
                        onValueChange = { companyText = it },
                        label = { Text("출판사 입력") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color(0xFF3563E9),
                            unfocusedBorderColor = Color.LightGray,
                            containerColor = Color(0xFFF6F8FB)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("장르 선택", style = MaterialTheme.typography.titleSmall.copy(color = Color(0xFF3563E9)))

                    // Chip 스타일 장르 선택: 3개씩 2줄
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        genreOptions.chunked(3).forEach { rowOptions ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowOptions.forEach { option ->
                                    val isSelected = selectedGenre == option
                                    Surface(
                                        color = if (isSelected) Color(0xFFB2C6FF) else Color(0xFFF0F3FA),
                                        shape = RoundedCornerShape(20.dp),
                                        border = BorderStroke(1.dp, if (isSelected) Color(0xFF3563E9) else Color.LightGray),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { selectedGenre = option }
                                    ) {
                                        Text(
                                            text = option,
                                            color = if (isSelected) Color(0xFF3563E9) else Color.Gray,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        )
                                    }
                                }
                                // 빈 칸 맞추기 (예: 5개면 마지막줄 2칸)
                                repeat(3 - rowOptions.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "검색 결과",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF3563E9),
                modifier = Modifier.padding(start = 22.dp, bottom = 8.dp)
            )
        }

        if (advancedFilteredBooks.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(46.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "검색 결과가 없습니다.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.LightGray)
                        )
                    }
                }
            }
        } else {
            items(advancedFilteredBooks) { book ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 10.dp)
                        .clickable {
                            vm.book = book
                            vm.navigate("detail")
                        },
                    elevation = CardDefaults.cardElevation(4.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FBFF))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter("http://23.21.136.176:8080/${book.imageUrl}"),
                            contentDescription = "${book.title} 이미지",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = book.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "작가: ${book.author}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF6E8BB7)
                            )
                            Text(
                                text = "장르: ${book.genre}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF91A7C7)
                            )
                            Text(
                                text = "가격: ${book.price}원",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF1A73E8))
                            )
                        }
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = "상세", tint = Color(0xFF3563E9), modifier = Modifier.size(28.dp))
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(vm: vm, userViewModel: UserViewModel) {
    val user = userViewModel.loggedInUser
    val context = LocalContext.current
    var commentText by rememberSaveable { mutableStateOf("") }
    val comments = userViewModel.bookComments.value
    var selectedRating by rememberSaveable { mutableIntStateOf(0) }
    val book = vm.book!!
    var bookScore by remember { mutableStateOf(book.score) }

    LaunchedEffect(book.id) {
        userViewModel.fetchBookComments(book.id)
        userViewModel.fetchAverageRating(book.id) { score ->
            bookScore = score
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(0.dp)
    ) {
        // 📕 책 상세/이미지/버튼
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 6.dp),
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = rememberAsyncImagePainter("http://23.21.136.176:8080/${book.imageUrl}"),
                        contentDescription = "${book.title} 이미지",
                        modifier = Modifier
                            .size(110.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFE0E6ED), shape = RoundedCornerShape(12.dp))
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = book.title,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("저자: ${book.author}", style = MaterialTheme.typography.labelLarge)
                        Text("출판사: ${book.publisher}", style = MaterialTheme.typography.labelLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "₩${book.price}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color(0xFF4285F4), fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            repeat(5) { idx ->
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = null,
                                    tint = if (idx <= bookScore!!.toInt()) Color(0xFFFFC107) else Color(0xFFE0E0E0),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                "(${String.format("%.1f", bookScore)})",
                                color = Color.Gray,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { vm.navigate("bill") },
                        enabled = vm.ishere,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4))
                    ) {
                        Text("구매하기", color = Color.White)
                    }
                    Button(
                        onClick = {
                            user?.tid?.let { userId ->
                                userViewModel.addToCart(userId, book.id, 1) { success ->
                                    val msg = if (success) "장바구니에 추가되었습니다." else "장바구니 추가 실패."
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        enabled = vm.ishere,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34A853))
                    ) {
                        Text("장바구니", color = Color.White)
                    }
                }
            }
        }

        // ⭐️ 평점 영역
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 5.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row {
                        (1..5).forEach { score ->
                            IconButton(
                                onClick = { selectedRating = score },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = "$score 점",
                                    tint = if (score <= selectedRating) Color(0xFFFFC107) else Color(0xFFE0E0E0),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                    Button(
                        onClick = {
                            user?.tid?.let { userId ->
                                userViewModel.rateBook(userId, book.id, selectedRating) { success ->
                                    if (success) {
                                        userViewModel.fetchAverageRating(book.id) { score -> bookScore = score }
                                        Toast.makeText(context, "평점을 주셔서 감사합니다!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        enabled = vm.ishere && selectedRating > 0,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF4B400))
                    ) {
                        Text("등록", color = Color.White)
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // 💬 댓글 타이틀
        item {
            Text(
                text = "댓글",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(start = 18.dp, top = 8.dp, bottom = 4.dp)
            )
        }

        // 📝 댓글 목록
        items(comments) { comment ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp, horizontal = 16.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = comment.user.name,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF4285F4)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = comment.content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black
                        )
                    }
                    if (vm.ishere && comment.user.tid == user!!.tid) {
                        IconButton(
                            onClick = {
                                userViewModel.deleteComment(comment.id, user.tid!!) {
                                    userViewModel.fetchBookComments(book.id)
                                }
                            },
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "삭제",
                                tint = Color(0xFFE53935)
                            )
                        }
                    }
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp,end=16.dp, bottom = 12.dp),
                elevation = CardDefaults.cardElevation(3.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        label = { Text("댓글을 입력하세요") },
                        modifier = Modifier.weight(0.7f),
                        singleLine = false,
                        maxLines = 3,
                        shape = RoundedCornerShape(10.dp),
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color(0xFFF6F8FB)
                        ),
                        enabled = user != null
                    )
                    Button(
                        onClick = {
                            if (commentText.isNotBlank() && user != null) {
                                userViewModel.addComment(
                                    bookId = book.id,
                                    userId = user.tid!!,
                                    content = commentText
                                ) {
                                    if (it) {
                                        commentText = ""
                                        userViewModel.fetchBookComments(book.id)
                                    }
                                }
                            }
                        },
                        enabled = vm.ishere,
                        modifier = Modifier.weight(0.3f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34A853))
                    ) {
                        Text("등록", color = Color.White)
                    }
                }
            }
        }
    }
}


@Composable
fun ManageScreen(vm: vm, userViewModel: UserViewModel) {
    val user = userViewModel.loggedInUser
    val context = LocalContext.current
    val deepBlue = Color(0xFF4472C4) // 강조 컬러
    val softCard = Color.White.copy(alpha = 0.85f)

    var showDialog by remember { mutableStateOf(false) }
    var passwordInput by rememberSaveable { mutableStateOf("") }
    var errorMessage by rememberSaveable { mutableStateOf("") }

    user?.let { u ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = softCard),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                "내 정보",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = deepBlue
                            )

                            Spacer(modifier = Modifier.weight(1f)) // 텍스트와 아이콘 사이 공간 확보

                            Icon(
                                imageVector = Icons.Default.Create,
                                contentDescription = "내 정보 수정",
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable { vm.navigate("editInfo") }
                                    .padding(end = 4.dp), // 오른쪽 패딩
                                tint = deepBlue
                            )
                        }

                        HorizontalDivider(thickness = 1.dp, color = deepBlue.copy(alpha = 0.3f))

                        val gen = if(u.gender=="F") "여성" else "남성"
                        val userInfo = listOf(
                            "아이디: ${u.id}",
                            "이름: ${u.name}",
                            "나이: ${u.age}",
                            "생년월일: ${u.birthdate}",
                            "성별: ${gen}",
                            "이메일: ${u.email}",
                            "결제카드: ${u.cardType}",
                            "카드번호: ${u.cardNumber}",
                            "결제계좌: ${u.bank}",
                            "주소: ${u.address}",
                            "전화번호: ${u.phone}"
                        )

                        userInfo.forEach { info ->
                            Text(text = info, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F4FF)),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("기능 메뉴",

                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = deepBlue)
                        HorizontalDivider(thickness = 1.dp, color = deepBlue.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(12.dp))

                        val buttonsRow1 = listOf(
                            "장바구니" to { vm.navigate("cart") },
                            "구매내역" to { vm.navigate("purchased") },
                            "쿠폰함" to { vm.navigate("coupon") }
                        )

                        val buttonsRow2 = listOf(
                            "댓글내용" to { vm.navigate("reply") },
                            "관심분야 설정" to { vm.navigate("favorite") }
                        )

                        val buttonsRow3 = listOf(
                            "회원탈퇴" to { showDialog = true }
                        )

                        @Composable
                        fun RowButtonRow(buttons: List<Pair<String, () -> Unit>>, special: String? = null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                buttons.forEach { (label, action) ->
                                    Button(
                                        onClick = action,
                                        modifier = Modifier.weight(1f),
                                        colors = if (label == special)
                                            ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                        else
                                            ButtonDefaults.buttonColors(containerColor = deepBlue, contentColor = Color.White)
                                    ) {
                                        Text(label)
                                    }
                                }
                            }
                        }

                        RowButtonRow(buttonsRow1)
                        Spacer(modifier = Modifier.height(10.dp))
                        RowButtonRow(buttonsRow2)
                        Spacer(modifier = Modifier.height(10.dp))
                        RowButtonRow(buttonsRow3, special = "회원탈퇴")
                    }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("회원 탈퇴", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("회원 탈퇴를 진행하려면 비밀번호를 입력하세요.")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("비밀번호") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (errorMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(errorMessage, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    userViewModel.deleteUser(user!!.id, passwordInput) { success, message ->
                        if (success) {
                            clearUserId(context)
                            vm.ishere=false
                            vm.navigate("home")
                        } else {
                            errorMessage = message ?: "회원 탈퇴에 실패했습니다."
                        }
                    }
                }) {
                    Text("확인")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDialog = false }) {
                    Text("취소")
                }
            }
        )
    }
}


@SuppressLint("NewApi")
@Composable
fun BillScreen(vm: vm, userViewModel: UserViewModel) {
    val user = userViewModel.loggedInUser
    val book = vm.book!!

    var selectedCoupon by remember { mutableStateOf<Coupon?>(null) } // 선택된 쿠폰
    var selectedCouponKey by rememberSaveable { mutableStateOf<Long?>(null) } // 선택된 쿠폰 ID
    val basePrice = book.price
    val randomDays = (1..2).random().toLong()
    val shippingDate = remember { LocalDate.now().plusDays(randomDays) }

    val cardOptions = listOf("신한", "농협", "삼성")
    var cardType by remember { mutableStateOf(user?.cardType) }
    var cardNumber by remember { mutableStateOf(user?.cardNumber) }
    var bank by remember { mutableStateOf(user?.bank) }
    val userCoupons = userViewModel.userCoupons.value.filter { !it.isUsed } // isUsed가 false인 쿠폰만 필터링
    val context = LocalContext.current

    LaunchedEffect(user?.tid) {
        user?.tid?.let { userViewModel.fetchUserCoupons(it) }
    }

    val finalPrice = if (selectedCoupon != null) {
        val discount = basePrice * selectedCoupon!!.discountPercent / 100 // 할인액
        (basePrice - discount).coerceAtLeast(0.0) // 할인 후 가격 (0원 이하 방지)
    } else basePrice

    LazyColumn {
        item {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text("결제 화면", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))

                Text("배송지: ${user?.address}")
                Spacer(modifier = Modifier.height(16.dp))

                Text("배송 예정일: $shippingDate")
                Spacer(modifier = Modifier.height(16.dp))

                Text("상품명: ${book.title} / 가격: ${book.price}원")
                Spacer(modifier = Modifier.height(16.dp))

                // 카드 선택
                Text("결제 카드 선택:")
                cardOptions.forEach { cardOption ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = (cardType == cardOption),
                            onClick = { cardType = cardOption }
                        )
                        Text(cardOption)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = cardNumber ?: "",
                    onValueChange = { cardNumber = it },
                    label = { Text("카드번호 입력") },
                    modifier = Modifier.fillMaxWidth()
                )

                TextField(
                    value = bank ?: "",
                    onValueChange = { bank = it },
                    label = { Text("은행 계좌 입력") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("보유 쿠폰:")
                if (userCoupons.isEmpty()) {
                    Text("사용 가능한 쿠폰이 없습니다.")
                } else {
                    userCoupons.forEach { coupon ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = (selectedCouponKey == coupon.id),
                                onClick = {
                                    selectedCouponKey = coupon.id
                                    selectedCoupon = coupon
                                }
                            )
                            Text("${coupon.code} - ${(coupon.discountPercent).toInt()}% 할인")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))


                Text("최종 결제 금액: ${finalPrice.toInt()} 원", style = MaterialTheme.typography.titleMedium)

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = {
                    user?.tid?.let { userId ->
                        userViewModel.placeOrder(userId, book.id, finalPrice, selectedCoupon?.code,1) { orderSuccess, orderId ->
                            if (orderSuccess && orderId != null) {
                                userViewModel.processPayment(orderId, "카드", userId, selectedCoupon?.code) { paymentSuccess ->
                                    if (paymentSuccess) {
                                        vm.navigate("home")
                                        Toast.makeText(context, "구매 성공!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Log.e("checkDDD", "결제 실패")
                                    }
                                }
                            } else {
                                Log.e("checkDDD", "주문 실패  ${selectedCoupon?.id?.toInt()}")
                            }
                        }
                    }
                }) {
                    Text("결제하기")
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { vm.navigate("detail") }) {
                    Text("취소")
                }
            }
        }
    }
}


@Composable
fun EditInfoScreen(vm: vm, userViewModel: UserViewModel
) {
    val user = userViewModel.loggedInUser!!
    var name by rememberSaveable { mutableStateOf(user.name) }
    var bank by rememberSaveable { mutableStateOf(user.bank?:"") }
    var age by rememberSaveable { mutableStateOf(user.age) }
    var gender by rememberSaveable { mutableStateOf(user.gender) }
    var email by rememberSaveable { mutableStateOf(user.email) }
    var cardType by rememberSaveable { mutableStateOf(user.cardType ?: "") }
    var cardNumber by rememberSaveable { mutableStateOf(user.cardNumber ?: "") }
    var address by rememberSaveable { mutableStateOf(user.address) }
    var phone by rememberSaveable { mutableStateOf(user.phone) }
    var birthDate by rememberSaveable { mutableStateOf(user.birthdate) }

    var currentPassword by rememberSaveable { mutableStateOf("") }

    var isLoading by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf("") }



    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .padding(16.dp)
    ) {
        item {
            Text(text = "회원정보 수정", style = MaterialTheme.typography.titleLarge)
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text("아이디: ${user.id}")
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("이름") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = age,
                onValueChange = { newValue -> age = newValue },
                label = { Text("나이") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("성별: ")
                RadioButton(
                    selected = (gender == "M"),
                    onClick = { gender = "M" }
                )
                Text("남")
                Spacer(modifier = Modifier.width(8.dp))
                RadioButton(
                    selected = (gender == "F"),
                    onClick = { gender = "F" }
                )
                Text("녀")
            }
        }
        item {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("이메일") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text("결제카드:")
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = (cardType == "삼성"),
                    onClick = { cardType = "삼성" }
                )
                Text("삼성")
                Spacer(modifier = Modifier.width(8.dp))
                RadioButton(
                    selected = (cardType == "신한"),
                    onClick = { cardType = "신한" }
                )
                Text("신한")
                Spacer(modifier = Modifier.width(8.dp))
                RadioButton(
                    selected = (cardType == "농협"),
                    onClick = { cardType = "농협" }
                )
                Text("농협")
            }
        }
        item {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = cardNumber,
                onValueChange = { cardNumber = it },
                label = { Text("카드번호") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = bank,
                onValueChange = {bank = it },
                label = { Text("결제 계좌") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }
        // 주소 입력
        item {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("주소") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("전화번호") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = birthDate,
                onValueChange = { birthDate = it },
                label = { Text("생년월일") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = currentPassword,
                onValueChange = { currentPassword = it },
                label = { Text("현재 비밀번호 확인") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
        }
        // 저장 버튼 및 오류 메시지 처리
        item {
            Spacer(modifier = Modifier.height(16.dp))
            if (errorMessage.isNotEmpty()) {
                Text(errorMessage, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    if (currentPassword != user.password) {
                        errorMessage = "비밀번호가 일치하지 않습니다."
                        return@Button
                    }
                    isLoading = true

                    try {
                        val updatedUser = user.copy(
                            name = name,
                            age = age,
                            gender = gender,
                            email = email,
                            cardType = cardType,
                            cardNumber = cardNumber,
                            address = address,
                            phone = phone,
                            birthdate = birthDate
                            ,bank = bank
                        )

                        userViewModel.updateUserInfo(updatedUser, currentPassword) { success, msg ->
                            isLoading = false
                            if (success) {
                                userViewModel.loggedInUser = updatedUser
                                vm.navigate("manage")
                            } else {
                                errorMessage = msg ?: "정보 수정에 실패하였습니다."
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("checkDDD", "user.copy() 실행 중 오류 발생: ${e.message}")
                        errorMessage = "오류 발생: ${e.message}"
                        isLoading = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("저장")
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { vm.navigate("manage") }, modifier = Modifier.fillMaxWidth()) {
                Text("취소")
            }
        }
    }
}

@Composable
fun FavoriteScreen(vm: vm, userViewModel: UserViewModel) {
    val user = userViewModel.loggedInUser
    val userId = user?.tid ?: return

    val userInterests by userViewModel.userInterests

    // 장르 목록
    val allGenres = listOf("고전", "디스토피아", "동화", "풍자", "전후소설")
    val genreStates = remember {
        mutableStateMapOf<String, Boolean>().apply {
            allGenres.forEach { genre -> this[genre] = false }
        }
    }

    // 관심 장르 로드 후 체크 상태 반영
    LaunchedEffect(userInterests) {
        allGenres.forEach { genre ->
            genreStates[genre] = userInterests.contains(genre)
        }
    }

    LaunchedEffect(Unit) {
        userViewModel.fetchUserInterests(userId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "관심 분야 설정",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        allGenres.chunked(2).forEach { rowGenres ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                rowGenres.forEach { genre ->
                    val checked = genreStates[genre] ?: false
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, if (checked) Color(0xFF1A73E8) else Color.LightGray, RoundedCornerShape(16.dp))
                            .clickable {
                                val newChecked = !checked
                                genreStates[genre] = newChecked
                                if (newChecked) userViewModel.addUserInterest(userId, genre)
                                else userViewModel.deleteUserInterest(userId, genre)
                            },
                        color = if (checked) Color(0xFF1A73E8) else Color.White
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (checked) {
                                Icon(Icons.Default.Check, contentDescription = "선택됨", tint = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(genre, color = if (checked) Color.White else Color.Black)
                        }
                    }
                }

                // 홀수 개일 때 여백 채우기
                if (rowGenres.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                userViewModel.fetchUserInterests(userId)
                vm.navigate("manage")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("저장")
        }
    }
}

@Composable
fun CouponScreen(userViewModel: UserViewModel) {
    val user = userViewModel.loggedInUser!!
    val userCoupons = userViewModel.userCoupons.value

    LaunchedEffect(user.tid) {
        userViewModel.fetchUserCoupons(user.tid!!)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
           //.background(Color(0xFFF8F9FA))
            .padding(16.dp)
    ) {
        Text(
            text = "🎁 쿠폰함",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (userCoupons.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.nocoupon),
                    contentDescription = "쿠폰 없음",
                    modifier = Modifier.size(200.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "보유 쿠폰이 없습니다.",
                    style = MaterialTheme.typography.bodyLarge.copy(color = Color.Gray)
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(userCoupons) { coupon ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        tonalElevation = 2.dp,
                        color = if (coupon.isUsed) Color(0xFFE0E0E0) else Color(0xFFFFF3E0),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "${coupon.code} - ${coupon.discountPercent.toInt()}% 할인",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (coupon.isUsed) Color.DarkGray else Color(0xFFEF6C00)
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "최소 주문 금액: ${coupon.minOrderAmount}원",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.DarkGray
                            )
                            Text(
                                text = "유효기간: ${coupon.expiryDate ?: "무기한"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.DarkGray
                            )
                            if (coupon.isUsed) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "✅ 사용됨",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = Color.Red,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CartScreen(vm: vm, userViewModel: UserViewModel) {
    val user = userViewModel.loggedInUser!!
    val context = LocalContext.current

    val userCartItems = userViewModel.userCart.value
    val userCoupons = userViewModel.userCoupons.value.filter { !it.isUsed }

    var couponSelectionVisible by rememberSaveable { mutableStateOf<Long?>(null) }
    var selectedCoupon by rememberSaveable { mutableStateOf<Coupon?>(null) }

    LaunchedEffect(user.tid!!) {
        userViewModel.fetchCart(user.tid)
        userViewModel.fetchUserCoupons(user.tid)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "장바구니",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (userCartItems.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(userCartItems) { cartItem ->
                    val book = cartItem.book

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(6.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = book.title,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = "${book.price}원 x ${cartItem.quantity}개",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        userViewModel.updateLocalCartAfterRemove(cartItem.id)
                                        userViewModel.removeCartItem(user.tid, book.id) {
                                            Toast.makeText(context, "삭제 완료", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("삭제")
                                }

                                Button(
                                    onClick = {
                                        couponSelectionVisible = book.id
                                        selectedCoupon = null
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("구매하기")
                                }
                            }

                            if (couponSelectionVisible == book.id) {
                                Spacer(modifier = Modifier.height(12.dp))

                                if (userCoupons.isEmpty()) {
                                    Text("보유 쿠폰이 없습니다.", color = Color.Gray)
                                } else {
                                    Text("사용할 쿠폰을 선택하세요:", fontWeight = FontWeight.SemiBold)
                                    Column(modifier = Modifier.padding(top = 8.dp)) {
                                        userCoupons.forEach { coupon ->
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                RadioButton(
                                                    selected = (selectedCoupon == coupon),
                                                    onClick = { selectedCoupon = coupon }
                                                )
                                                Text("${coupon.code} - ${coupon.discountPercent.toInt()}% 할인")
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = {
                                            couponSelectionVisible = null
                                            selectedCoupon = null
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("취소")
                                    }
                                    Button(
                                        onClick = {
                                            val finalPrice = if (selectedCoupon != null) {
                                                (book.price * (1 - selectedCoupon!!.discountPercent / 100.0)).toInt().toDouble()
                                            } else book.price

                                            userViewModel.placeOrder(
                                                userId = user.tid,
                                                bookId = book.id,
                                                Price = finalPrice,
                                                Coupon = selectedCoupon?.code,
                                                cartItem.quantity
                                            ) { success, orderId ->
                                                if (success && orderId != null) {
                                                    userViewModel.processPayment(orderId, "카드", user.tid, selectedCoupon?.code) { paymentSuccess ->
                                                        if (paymentSuccess) vm.navigate("home")
                                                    }
                                                    Toast.makeText(context, "구매 완료!", Toast.LENGTH_SHORT).show()
                                                    userViewModel.removeCartItem(user.tid, book.id) {
                                                        userViewModel.fetchCart(user.tid)
                                                    }
                                                } else {
                                                    Toast.makeText(context, "구매 실패", Toast.LENGTH_SHORT).show()
                                                }
                                            }

                                            couponSelectionVisible = null
                                            selectedCoupon = null
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("결제하기")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    userViewModel.clearCart(user.tid) {
                        if (it) userViewModel.fetchCart(user.tid)
                        Toast.makeText(context, "장바구니를 비웠습니다.", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350))
            ) {
                Text("장바구니 비우기", color = Color.White)
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("장바구니가 비었습니다.", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
            }
        }

    }
}


@Composable
fun PurchasedScreen(userViewModel: UserViewModel) {
    val context = LocalContext.current
    val user = userViewModel.loggedInUser!!
    val userOrders by userViewModel.userOrders

    LaunchedEffect(user.tid!!) {
        userViewModel.fetchUserOrders(user.tid)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
           // .background(Color(0xFFF5F7FA))
            .padding(16.dp)
    ) {
        Text(
            text = "구매 내역",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            if (userOrders.isNotEmpty()) {
                items(userOrders) { order ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("주문 ID: ${order.id}", style = MaterialTheme.typography.titleSmall)
                            Text("결제 상태: ${order.status}", style = MaterialTheme.typography.bodySmall)

                            Spacer(modifier = Modifier.height(12.dp))

                            HorizontalDivider(thickness = 2.dp, color = Color(0xFF4472C4))
                            Spacer(modifier = Modifier.height(3.dp))
                            order.orderItems?.forEach { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${item.title} (${item.quantity}개)",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "${item.price}원",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }

                                    Text(
                                        text = "배송 상태: {${order.delivery?.deliveryStatus}}",
                                        style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                                    )

                            }

                            if (order.status == "결제 완료" && order.discountedAmount == order.totalAmount) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        userViewModel.processRefund(order.id, user.tid) { refundSuccess ->
                                            if (refundSuccess) {
                                                Toast.makeText(context, "환불 성공! 주문 ID: ${order.id}", Toast.LENGTH_SHORT).show()
                                                userViewModel.fetchUserOrders(user.tid)
                                            } else {
                                                Log.e("checkDDD", "환불 실패")
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("환불", color = Color.White)
                                }
                            }
                        }
                    }
                }
            } else {
                item {
                    Text(
                        text = "구매 내역이 없습니다.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}


@Composable
fun ReplyScreen(vm: vm, userViewModel: UserViewModel) {
    val user = userViewModel.loggedInUser!!
    val userComments = userViewModel.userComments.value

    LaunchedEffect(user.tid) {
        userViewModel.fetchUserComments(user.tid!!)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "나의 댓글",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (userComments.isNotEmpty()) {
            LazyColumn {
                items(userComments) { comment ->
                    val book = comment.book

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("댓글 내용: ${comment.content}")  // ✅ 댓글 내용 표시
                            Text("책 제목: ${book.title}")  // ✅ 책 제목 표시
                        }
                        Button(onClick = {
                            vm.book = book
                            vm.navigate("detail")
                        }) {
                            Text("이동")
                        }
                    }
                    HorizontalDivider()
                }
            }
        } else {
            Text("작성한 댓글이 없습니다.")
        }

    }
}

@Composable
fun RegisterScreen(vm: vm, userViewModel: UserViewModel) {
    var idOkay by rememberSaveable { mutableStateOf(false) }
    var userId by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var name by rememberSaveable { mutableStateOf("") }
    var age by rememberSaveable { mutableStateOf("") }
    var gender by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var birthDate by rememberSaveable { mutableStateOf("") }
    var errorMessage by rememberSaveable { mutableStateOf("") }
    var errorMessage2 by rememberSaveable { mutableStateOf("") }
    var address by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }

    var isLoading by rememberSaveable { mutableStateOf(false) }

    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextField(
                    value = userId,
                    onValueChange = {
                        userId = it
                        idOkay = false
                    },
                    label = { Text("아이디") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(6.dp))
                OutlinedButton(
                    onClick = {
                        if (userId.isBlank()) {
                            errorMessage2 = "아이디를 입력하세요."
                            idOkay = false
                            return@OutlinedButton
                        }

                        userViewModel.checkDuplicateUserId(userId) { available, msg ->
                            if (available) {
                                idOkay = true
                                errorMessage2 = "사용 가능한 아이디입니다."
                            } else {
                                idOkay = false
                                errorMessage2 = msg ?: "이미 사용중인 아이디입니다."
                            }
                        }
                    },
                    modifier = Modifier.height(54.dp) // TextField와 높이 맞추기
                ) {
                    Text("중복확인", fontSize = 14.sp)
                }
            }
            if (idOkay) {
                Text("✔ 사용 가능한 아이디입니다.", color = Color(0xFF388E3C), fontSize = 13.sp)
            } else if (errorMessage2.isNotEmpty() && !idOkay) {
                Text("이미 존재하는 아이디 입니다.", color = Color.Red, fontSize = 13.sp)
            }
        }

        item {
            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("비밀번호") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            TextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("비밀번호 확인") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("이름") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            TextField(
                value = age,
                onValueChange = { newValue ->
                    if (newValue.all { it.isDigit() }) {
                        age = newValue
                    }
                },
                label = { Text("나이") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("성별")
                RadioButton(
                    selected = gender == "M",
                    onClick = { gender = "M" }
                )
                Text("남")
                RadioButton(
                    selected = gender == "F",
                    onClick = { gender = "F" }
                )
                Text("녀")
            }
        }

        item {
            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("이메일") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Text(
                text = birthDate.ifEmpty { "생년월일 선택" },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(
                        MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.medium
                    )
                    .clickable {
                        DatePickerDialog(
                            context,
                            { _, selectedYear, selectedMonth, selectedDay ->
                                birthDate = "$selectedYear-${selectedMonth + 1}-$selectedDay"
                            },
                            year,
                            month,
                            day
                        ).show()
                    }
                    .padding(16.dp),
                color = if (birthDate.isEmpty()) Color.Gray else Color.Black
            )
        }

        item {
            TextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("주소") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            TextField(
                value = phone,
                onValueChange = { newValue ->
                    if (newValue.all { it.isDigit() }) {
                        phone = newValue
                    }
                },
                label = { Text("-없이 전화번호만 입력") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        item {
            if (errorMessage.isNotEmpty()) {
                Text(errorMessage, color = MaterialTheme.colorScheme.error)
            }
        }

        item {
            Button(
                onClick = {
                        if (password != confirmPassword) {
                        errorMessage = "비밀번호가 일치하지 않습니다."
                    } else if (!idOkay) {
                            errorMessage = "아이디 중복확인을 해주세요."
                            return@Button
                        }else {
                        isLoading = true
                        errorMessage = ""

                        val newUser = UserInfo(
                            id = userId, password = password, name = name,
                            age = age,
                            gender = gender, email = email,
                            address = address, phone = phone, birthdate = birthDate
                        )

                        userViewModel.register(newUser) { success,error ->
                            isLoading = false
                            if (success) {
                                userViewModel.infofind(userId){userinfo->
                                    userinfo?.tid?.let { userViewModel.issueWelcomeCoupon(it) } }
                                vm.navigate("login")
                            } else {
                                errorMessage = error
                            } } } },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else { Text("회원가입") } } }

        } }



