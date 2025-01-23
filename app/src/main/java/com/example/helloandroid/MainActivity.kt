package com.example.helloandroid

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.helloandroid.ui.theme.HelloAndroidTheme
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import android.app.DatePickerDialog
import android.content.Context
import android.os.Debug
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import java.time.LocalDate
import java.util.*
import kotlin.math.log
import kotlin.random.Random
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HelloAndroidTheme {

                Surface(color = MaterialTheme.colorScheme.background
                ) {
                    LoginApp()
                }
            }
        }
    }
}

class vm: ViewModel() {
    var Screen by mutableStateOf("home")
    fun navigate(screen: String) { Screen = screen }

    var loggedInUser by mutableStateOf<UserInfo?>(null)
    fun login(user:UserInfo){ loggedInUser=user }
    fun logout(){ loggedInUser=null }

    var users = mutableStateMapOf<String, UserInfo>()

    var book by  mutableStateOf<BookInfo?>(null)

}


data class UserInfo(val id: String, val password: String, val name: String,
                    val age: String, val gender: String, val email: String,
                    val cardType:String,val cardNumber:String,val address:String,
                    val phone: String,
                    val birthdate: String, var purchasedBooks: MutableList<BookInfo> = mutableListOf(),
                    var cartBooks: MutableList<BookInfo> = mutableListOf(),
                    var comments: MutableList<Comment> = mutableListOf(),
                    var favorits: MutableList<String> = mutableListOf(),
                    var coupons: MutableMap<Int,Coupon> = mutableMapOf(),
                    var appreciated: MutableMap<String, Int> = mutableMapOf(),
                    var searchHistory: MutableList<String> = mutableListOf(),
)
data class BookInfo(val title: String, val author: String, val price: Int, val imageResId: Int?,
                    val publish:Int,val company:String,
                    val genre:List<String>, var com:Int,
                    var ratingSum: Int = 0, var ratingCount: Int = 0, var rating:Double=0.0,   )
data class Comment(val userId: String, val text: String,var Books: MutableList<BookInfo> = mutableListOf())
data class Coupon(val name: String, val description:String,val discountRate: Double)

@Composable
fun LoginApp() {
    val vm: vm = viewModel()
    var comments = rememberSaveable { mutableStateOf(mutableMapOf<String, Comment>()) }
    vm.users["1"] =  UserInfo("1", "1", "김희원", "22", "남", "hansung@naver.com", "신한","1","2","3","2000-1-1")
    vm.users["익명"] =  UserInfo("익명", "1", "김희원", "22", "남", "hansung@naver.com", "신한","1","2","3","2000-1-1")
    vm.users["1"]?.coupons?.set(1, Coupon("30% 할인쿠폰","가입 혜택!", 0.3))
    vm.users["1"]?.coupons?.set(2, Coupon("50% 할인쿠폰","가입 혜택!", 0.5))
    vm.users["1"]?.coupons?.set(3, Coupon("50% 할인쿠폰","가입 혜택!", 0.5))

    var bookList by rememberSaveable {
        mutableStateOf(
            listOf(
                BookInfo("어린 왕자", "생텍쥐페리", 5000, R.drawable.book_image_1,1943,"반올림" ,listOf("동화","고전"),0),
                BookInfo("데미안", "헤르만 헤세", 8000, R.drawable.book_image_2,1919,"책벌레",listOf("전후소설"),0),
                BookInfo("더블린 사람들", "제임스 조이스", 6000, R.drawable.book_image_3,1914,"반올림",listOf("고전"),0),
                BookInfo("걸리버 여행기", "조너선 스위프트", 5000, R.drawable.book_image_4,1726,"민음사",listOf("풍자"),0),
                BookInfo("동물농장", "조지 오웰", 9000, R.drawable.book_image_5,1945,"책벌레",listOf("풍자","디스토피아"),0),
            )
        )
    }

    when (vm.Screen) {
        "login" -> LoginScreen()
        "register" -> RegisterScreen()
        "home" -> HomeScreen(bookList = bookList)
        "advanced"-> AdvancedScreen(bookList=bookList)
        "detail" -> vm.book?.let {
            DetailScreen(
                book = it,
                onNavigateToBill = { book -> vm.book = book
                                     vm.navigate( "bill") },
                comments = comments.value,
                onCommentAdd = { userId, text, book ->
                    val newComment = Comment(userId, text, mutableListOf(book))
                    comments.value[text]=newComment
                    vm.loggedInUser?.comments?.add(newComment)
                },
                onCommentDelete ={ userId, comment ->
                    if (comment.userId == userId) {
                        comments.value.remove(comment.text)
                        vm.loggedInUser?.comments?.remove(comment)
                    }
                },
            )
        }
        "bill" -> BillScreen()
        "manage" -> ManageScreen()
        "coupon" -> CouponScreen()
        "cart" -> CartScreen()
        "purchased" -> PurchasedScreen()
        "reset" -> ResetScreen()
        "reply" -> ReplyScreen()
        "favorite" -> FavoriteScreen()
        "editInfo" ->
            vm.loggedInUser?.let {
                EditInfoScreen(
                    user = it,
                    onSaveUserInfo = { updatedUser ->
                        vm.login( updatedUser)
                        vm.users[it.id] = updatedUser
                    }
                )
            }
    }
}

@Composable
fun LoginScreen() {
    val vm :vm = viewModel()
    var userId by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var errorMessage by rememberSaveable { mutableStateOf("") }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        TextField(
            value = userId,
            onValueChange = {userId=it},
            label = { Text("아이디") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = password,
            onValueChange = {password=it},
            label = { Text("비밀번호") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        if (errorMessage.isNotEmpty()) {
            Text(errorMessage, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val user = vm.users[userId]
                if (user != null && user.password == password) {
                    vm.login(user)
                    userId = ""
                    password = ""
                    errorMessage =""
                    vm.navigate("home")
                } else {
                    errorMessage ="다시 시도해 주세요"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("로그인")
        }

        Button(onClick= { vm.navigate("home") },
            modifier = Modifier.fillMaxWidth()){
            Text("뒤로가기")
        }

        Box( modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)){
            Row(
                modifier = Modifier.align(Alignment.TopEnd), // 오른쪽 위에 정렬
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = {vm.navigate("reset")},
                ) {
                    Text("비밀번호를 잊으셨나요?")
                } } } } }

@Composable
fun ResetScreen() {
    val vm:vm= viewModel()
    var userId by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var verificationPassed by rememberSaveable { mutableStateOf(false) }
    var verificationPassed2 by rememberSaveable { mutableStateOf(false) }
    var emailcheck by rememberSaveable { mutableStateOf("") }
    var newPassword1 by rememberSaveable { mutableStateOf("") }
    var newPassword2 by rememberSaveable { mutableStateOf("") }
    var errorMessage by rememberSaveable { mutableStateOf("") }
    val temppass ="12345"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        if (!verificationPassed) {
            TextField(
                value = userId,
                onValueChange = { userId = it },
                label = { Text("아이디") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("이메일") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val user = vm.users[userId]
                    if (user != null && user.email == email) {
                        verificationPassed = true
                        errorMessage = ""
                    } else {
                        errorMessage = "아이디 또는 이메일이 일치하지 않습니다."
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("확인")
            }

            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(errorMessage, color = MaterialTheme.colorScheme.error)
            }
        } else if(!verificationPassed2){
            TextField(
            value = emailcheck,
            onValueChange = { emailcheck = it },
            label = { Text("이메일로 전송된 5자리 입력") },
            modifier = Modifier.fillMaxWidth()
        )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (emailcheck==temppass) {
                        verificationPassed2 = true
                        errorMessage = ""
                    } else {
                        errorMessage = "전송된 번호와 일치하지 않습니다."
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("확인")
            }

            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(errorMessage, color = MaterialTheme.colorScheme.error)
            }
        }
        else {
            TextField(
                value = newPassword1,
                onValueChange = { newPassword1 = it },
                label = { Text("새로운 비밀번호") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = newPassword2,
                onValueChange = { newPassword2 = it },
                label = { Text("새로운 비밀번호 확인") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (newPassword1 == newPassword2) {
                        vm.users[userId] = vm.users[userId]!!.copy(password = newPassword1)
                        errorMessage = ""
                       vm.navigate("login")
                    } else {
                        errorMessage = "비밀번호가 일치하지 않습니다."
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("비밀번호 변경")
            }

            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(errorMessage, color = MaterialTheme.colorScheme.error)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick =  {vm.navigate("login")},
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("로그인")
        } } }

@Composable
fun HomeScreen(
    bookList: List<BookInfo>,
) {
    val vm: vm = viewModel()
    val user = vm.loggedInUser

    var searchText by rememberSaveable { mutableStateOf("") }
    var searchType by rememberSaveable { mutableStateOf("제목") }
    var dropdownExpanded by rememberSaveable { mutableStateOf(false) }
    var filteredBooks by rememberSaveable { mutableStateOf(bookList) }

    var filteredBooks1 by rememberSaveable { mutableStateOf(bookList) } // 검색 결과
    var filteredBooks2 by rememberSaveable { mutableStateOf(bookList) } // 장르 필터 결과

    val searchHistory = user?.searchHistory ?: mutableListOf()
    val focusManager = LocalFocusManager.current
    var isTextFieldFocused by rememberSaveable { mutableStateOf(false) }

    val genreOptions = listOf("전체", "풍자", "디스토피아", "고전", "전후소설", "동화")
    var selectedGenre by rememberSaveable { mutableStateOf("전체") }

    var selectedSort by rememberSaveable { mutableStateOf("댓글순") } // 정렬 기준
    var sortDropdownExpanded by rememberSaveable { mutableStateOf(false) }

    var selectedOrder by rememberSaveable { mutableStateOf("오름차순") } // 정렬 순서
    var orderDropdownExpanded by rememberSaveable { mutableStateOf(false) }

    fun getAutoCompleteSuggestions(input: String): List<String> {
        if (input.length < 1) return emptyList()
        return when (searchType) {
            "제목" -> {
                bookList
                    .map { it.title }
                    .filter { it.startsWith(input, ignoreCase = true) }
                    .distinct()
            }
            "작가" -> {
                bookList
                    .map { it.author }
                    .filter { it.startsWith(input, ignoreCase = true) }
                    .distinct()
            }
            else -> { // "출판사" 가정
                bookList
                    .map { it.company } // BookInfo에 company 필드가 있다고 가정
                    .filter { it.startsWith(input, ignoreCase = true) }
                    .distinct()
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (user == null || user.id == "익명") {
                    Text(
                        text = "로그인",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable {
                            vm.navigate("login")
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "회원가입",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable {
                            vm.navigate("register")
                        }
                    )
                } else {
                    Text(
                        text = "로그아웃",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable {
                            vm.logout()
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "회원정보관리",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable {
                            vm.navigate("manage")
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }


        item {
            Column {
                Box {
                    Row {
                        TextField(
                            value = searchText,
                            onValueChange = { searchText = it },
                            label = {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("검색어 입력")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                                .onFocusChanged { focusState ->
                                    isTextFieldFocused = focusState.isFocused
                                }
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Box {
                            OutlinedButton(onClick = { dropdownExpanded = true }) {
                                Text(text = searchType, color = Color.Black)
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "드롭다운",
                                    tint = Color.Black
                                )
                            }
                            DropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("제목") },
                                    onClick = {
                                        searchType = "제목"
                                        dropdownExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("작가") },
                                    onClick = {
                                        searchType = "작가"
                                        dropdownExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("출판사") },
                                    onClick = {
                                        searchType = "출판사"
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(onClick = {
                            val result = if (searchText.isBlank()) {
                                bookList
                            } else {
                                if (searchType == "제목") {
                                    bookList.filter {
                                        it.title.contains(searchText, ignoreCase = true)
                                    }
                                } else if (searchType == "작가") {
                                    bookList.filter {
                                        it.author.contains(searchText, ignoreCase = true)
                                    }
                                } else {
                                    bookList.filter {
                                        it.company.contains(searchText, ignoreCase = true)
                                    }
                                }
                            }
                            filteredBooks1 = result
                            focusManager.clearFocus(force = true)

                            if (searchText.isNotBlank()) {
                                user?.searchHistory?.remove(searchText)
                                user?.searchHistory?.add(0, searchText)
                            }

                            applyFinalFilterAndSort(
                                filteredBooks1,
                                filteredBooks2,
                                selectedSort,
                                selectedOrder
                            ) { resultList ->
                                filteredBooks = resultList
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "검색",
                                tint = Color.White
                            )
                        }
                    }

                    if (isTextFieldFocused && searchText.length >= 1) {
                        val autoCompleteList = getAutoCompleteSuggestions(searchText)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 64.dp)
                                .align(Alignment.TopStart),
                            elevation = CardDefaults.cardElevation(8.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            ) {
                                if (searchHistory.isNotEmpty()) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.End,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "검색 기록 전체 삭제",
                                            modifier = Modifier.clickable {
                                                user?.searchHistory?.clear()
                                                isTextFieldFocused = false
                                            },
                                            color = Color.Red
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))

                                    searchHistory.forEach { record ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    searchText = record
                                                    isTextFieldFocused = false
                                                }
                                        ) {
                                            Text(record)
                                            Spacer(Modifier.weight(1f))
                                            IconButton(onClick = {
                                                user?.searchHistory?.remove(record)
                                            }) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "삭제"
                                                )
                                            }
                                        }
                                    }
                                }

                                if (autoCompleteList.isNotEmpty() && searchHistory.isNotEmpty()) {
                                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                                }

                                if (autoCompleteList.isNotEmpty()) {
                                    Text("추천 검색", style = MaterialTheme.typography.titleMedium)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    autoCompleteList.forEach { suggestion ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    searchText = suggestion
                                                    isTextFieldFocused = false
                                                }
                                        ) {
                                            Text(
                                                suggestion,
                                                modifier = Modifier.padding(vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(onClick = { sortDropdownExpanded = true }) {
                            Text(selectedSort)
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = Color.Black
                            )
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
                                        applyFinalFilterAndSort(
                                            filteredBooks1,
                                            filteredBooks2,
                                            selectedSort,
                                            selectedOrder
                                        ) { resultList ->
                                            filteredBooks = resultList
                                        }
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        OutlinedButton(onClick = { orderDropdownExpanded = true }) {
                            Text(selectedOrder)
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = Color.Black
                            )
                        }
                        DropdownMenu(
                            expanded = orderDropdownExpanded,
                            onDismissRequest = { orderDropdownExpanded = false }
                        ) {
                            listOf("오름차순", "내림차순").forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        selectedOrder = option
                                        orderDropdownExpanded = false
                                        applyFinalFilterAndSort(
                                            filteredBooks1,
                                            filteredBooks2,
                                            selectedSort,
                                            selectedOrder
                                        ) { resultList ->
                                            filteredBooks = resultList
                                        }
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                vm.navigate("advanced")
                            },
                        ) {
                            Text("고급 검색")
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("장르:")
                        Spacer(modifier = Modifier.width(8.dp))

                        genreOptions.forEach { option ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) {
                                RadioButton(
                                    selected = (selectedGenre == option),
                                    onClick = {
                                        selectedGenre = option
                                        if (option == "전체") {
                                            filteredBooks2 = bookList
                                        } else {
                                            filteredBooks2 = bookList.filter { book ->
                                                book.genre.contains(option)
                                            }
                                        }
                                        applyFinalFilterAndSort(
                                            filteredBooks1,
                                            filteredBooks2,
                                            selectedSort,
                                            selectedOrder
                                        ) { resultList ->
                                            filteredBooks = resultList
                                        }
                                    }
                                )
                                Text(option)
                            }
                        }
                    }
                }
            }
        }

        items(filteredBooks) { book ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable {
                        if (vm.loggedInUser == null) {
                            vm.login(vm.users["익명"]!!)
                        }
                        vm.book = book
                        vm.navigate("detail")
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                book.imageResId?.let { resId ->
                    Image(
                        painter = painterResource(id = resId),
                        contentDescription = "${book.title} 이미지",
                        modifier = Modifier
                            .size(150.dp)
                            .padding(8.dp)
                    )
                } ?: Box(
                    modifier = Modifier
                        .size(150.dp)
                        .background(Color.Gray),
                    contentAlignment = Alignment.Center
                ) {
                    Text("이미지 없음")
                }

                Text(
                    text = "${book.title}(${book.com})  평점: ${"%.2f".format(book.rating)}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "저자: ${book.author} | 가격: ${book.price}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}


@Composable
fun AdvancedScreen(
    bookList: List<BookInfo>,
) {
    val vm: vm = viewModel()

    var authorText by rememberSaveable { mutableStateOf("") }
    var titleText by rememberSaveable { mutableStateOf("") }
    var companyText by rememberSaveable { mutableStateOf("") }

    val genreOptions = listOf("전체", "풍자", "디스토피아", "고전", "전후소설", "동화")
    var selectedGenre by rememberSaveable { mutableStateOf("전체") }

    var advancedFilteredBooks by rememberSaveable { mutableStateOf(bookList) }
LazyColumn {  item{
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "고급 검색",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )


        TextField(
            value = titleText,
            onValueChange = { titleText = it },
            label = { Text("제목 입력") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = authorText,
            onValueChange = { authorText = it },
            label = { Text("작가 입력") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = companyText,
            onValueChange = { companyText = it },
            label = { Text("출판사 입력") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("장르 선택:")
       LazyRow {
           items(genreOptions) { option ->
                    RadioButton(
                        selected = (selectedGenre == option),
                        onClick = { selectedGenre = option })
                    Text(option) } }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val results = bookList.filter { book ->
                val matchAuthor = authorText.isBlank() ||
                        book.author.contains(authorText, ignoreCase = true)
                val matchTitle = titleText.isBlank() ||
                        book.title.contains(titleText, ignoreCase = true)
                val matchCompany = companyText.isBlank() ||
                        book.company.contains(companyText, ignoreCase = true)
                val matchGenre = (selectedGenre == "전체") ||
                        (book.genre.contains(selectedGenre))
                matchAuthor && matchTitle && matchGenre&&matchCompany
            }
            advancedFilteredBooks = results
        }) {
            Text("검색")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (advancedFilteredBooks.isEmpty()) {
            Text("검색 결과가 없습니다.")
        } else {


            advancedFilteredBooks.forEach { book ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable {
                            if (vm.loggedInUser == null) {
                                vm.login(vm.users["익명"]!!)
                            }
                            vm.book = book
                            vm.navigate("detail")
                        },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    book.imageResId?.let { resId ->
                        Image(
                            painter = painterResource(id = resId),
                            contentDescription = "${book.title} 이미지",
                            modifier = Modifier
                                .size(150.dp)
                                .padding(8.dp)
                        )
                    } ?: Box(
                        modifier = Modifier
                            .size(150.dp)
                            .background(Color.Gray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("이미지 없음")
                    }

                    Text(
                        text = "${book.title}  | 작가: ${book.author}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text("장르: ${book.genre.joinToString(", ")}")
                    Text("가격: ${book.price}원")
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

           }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick ={ vm.navigate("home")}) {
            Text("뒤로가기")
        } }
}}
}

@Composable
fun DetailScreen(
    book: BookInfo,
    onNavigateToBill: (BookInfo) -> Unit,
    comments: MutableMap<String, Comment>,
    onCommentAdd: (String, String, BookInfo) -> Unit,
    onCommentDelete: (String, Comment) -> Unit
) {
    val vm: vm = viewModel()
    val user = vm.loggedInUser ?: return
    var commentText by rememberSaveable { mutableStateOf("") }
    var starRating by rememberSaveable { mutableStateOf(user.appreciated.getOrDefault(book.title, 0)) }
    var ratingSum by rememberSaveable { mutableStateOf(book.ratingSum) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            item {
                book.imageResId?.let { resId ->
                    Image(
                        painter = painterResource(id = resId),
                        contentDescription = "${book.title} 이미지",
                        modifier = Modifier
                            .size(200.dp)
                            .padding(8.dp)
                    )
                } ?: Box(
                    modifier = Modifier
                        .size(200.dp)
                        .background(Color.Gray),
                    contentAlignment = Alignment.Center
                ) {
                    Text("이미지 없음")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "${book.title}(${book.publish})",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text("저자: ${book.author}", style = MaterialTheme.typography.bodyMedium)
                Text("출판사: ${book.company}", style = MaterialTheme.typography.bodyMedium)
                Text("가격: ${book.price}원", style = MaterialTheme.typography.bodyMedium)

                Spacer(modifier = Modifier.height(16.dp))

                Row {
                    Button(
                        onClick = { onNavigateToBill(book) },
                        enabled = (user.id != "익명")
                    ) {
                        Text("구매하기")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            user.cartBooks.add(book)
                            Toast.makeText(context, "장바구니에 추가되었습니다.", Toast.LENGTH_SHORT).show()
                        },
                        enabled = (user.id != "익명")
                    ) {
                        Text("장바구니")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            items(comments.values.filter { it.Books.contains(book) }) { comment ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${comment.userId}: ${comment.text}")

                    if (comment.userId == user.id) {
                        Button(onClick = {
                            comments.remove(comment.text)
                            user.comments.remove(comment)
                            book.com--
                            onCommentDelete(user.id, comment)
                        }) {
                            Text("삭제")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            item {
                Text(
                    text = if (book.ratingCount == 0) {
                        "평점: 0.00 (총 0명)"
                    } else {
                        val average = book.ratingSum.toFloat() / book.ratingCount
                        val formatted = "%.2f".format(average)
                        book.rating = formatted.toDouble()
                        "평점: $formatted (총 ${book.ratingCount}명)"
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (user.id != "익명") {
                    TextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        label = { Text("댓글을 입력하세요") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = {
                                if (commentText.isNotBlank()) {
                                    onCommentAdd(user.id, commentText, book)
                                    commentText = ""
                                    book.com++
                                }
                            }
                        ) {
                            Text("등록")
                        }
                    }

                    StarRatingBar(
                        rating = starRating,
                        onRatingChanged = { newRating ->
                            starRating = newRating
                        }
                    )

                    Text("내가 줬던 평점: ${user.appreciated.getOrDefault(book.title, 0)}점")

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = {
                                if (user.appreciated.containsKey(book.title)) {
                                    ratingSum += starRating
                                    Toast.makeText(context, "평점이 수정되었습니다.", Toast.LENGTH_SHORT).show()

                                    book.ratingSum -= user.appreciated.getOrDefault(book.title, 0)
                                    book.ratingSum += starRating
                                    user.appreciated[book.title] = starRating
                                } else {
                                    ratingSum += starRating
                                    book.ratingSum += starRating
                                    book.ratingCount += 1
                                    user.appreciated[book.title] = starRating
                                    Toast.makeText(context, "평점을 주셔서 감사합니다!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Text("평점 등록")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = { vm.navigate("home") }) {
                    Text("뒤로가기")
                } } } } }

@Composable
fun ManageScreen() {
    val vm:vm= viewModel()
    vm.loggedInUser?.let { user ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background) // 배경색
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "내 정보",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Divider(thickness = 1.dp, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))

                        Text("아이디: ${user.id}")
                        Text("이름: ${user.name}")
                        Text("나이: ${user.age}")
                        Text("생년월일: ${user.birthdate}")
                        Text("성별: ${user.gender}")
                        Text("이메일: ${user.email}")
                        Text("결제카드: ${user.cardType}")
                        Text("결제카드번호: ${user.cardNumber}")
                        Text("주소: ${user.address}")
                        Text("전화번호: ${user.phone}")
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "기능 메뉴",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Divider(thickness = 1.dp, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { vm.navigate("cart") },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("장바구니")
                            }
                            Button(
                                onClick = {vm.navigate("purchased")},
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("구매내역")
                            }
                            Button(
                                onClick = {vm.navigate("coupon")},
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("쿠폰함")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {vm.navigate("reply")},
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("댓글내용")
                            }
                            Button(
                                onClick = {vm.navigate("favorite") },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("관심분야 설정")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {vm.navigate("editInfo")},
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("회원정보 수정")
                            }
                            Button(
                                onClick = { vm.users.remove(user.id)
                                    vm.logout()
                                    vm.navigate( "home")},
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("회원탈퇴", color = MaterialTheme.colorScheme.onError)
                            } } } } }

            item {
                OutlinedButton(
                    onClick = {vm.navigate("home")},
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("뒤로가기")
                }
            }
        }
    }
}

@SuppressLint("NewApi")
@Composable
fun BillScreen() {
    val vm: vm = viewModel()
    val book = vm.book!!

    var selectedCoupon by remember { mutableStateOf<Coupon?>(null) }
    var selectedCouponKey by rememberSaveable { mutableStateOf<Int?>(null) }
    val basePrice = book.price
    val randomDays = (1..2).random().toLong()
    val shippingDate = remember { LocalDate.now().plusDays(randomDays) }
    val finalPrice = if (selectedCoupon != null) {
        (basePrice * (1 - selectedCoupon!!.discountRate)).toInt()
    } else basePrice
    val cardOptions = listOf("신한", "농협", "삼성")
    var selectedCardType by remember { mutableStateOf(vm.loggedInUser?.cardType) }
    var cardNumber by remember { mutableStateOf(vm.loggedInUser?.cardNumber) }

    LazyColumn {
        item {
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)) {

                Text("결제 화면", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))

                Text("배송지: ${vm.loggedInUser?.address}")
                Spacer(modifier = Modifier.height(16.dp))

                Text("배송 예정일: $shippingDate")
                Spacer(modifier = Modifier.height(16.dp))

                Text("상품명: ${book.title} / 가격: ${book.price}원")
                Spacer(modifier = Modifier.height(16.dp))

                Text("결제 카드 선택:")
                cardOptions.forEach { cardOption ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = (selectedCardType == cardOption),
                            onClick = {
                                selectedCardType = cardOption
                            }
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

                Spacer(modifier = Modifier.height(16.dp))

                Text("보유 쿠폰: ")
                val couponMap = vm.loggedInUser?.coupons
                if (couponMap.isNullOrEmpty()) {
                    Text("쿠폰이 없습니다.")
                } else {
                    couponMap.forEach { (key, coupon) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = (selectedCouponKey == key),
                                onClick = {
                                    selectedCouponKey = key
                                    selectedCoupon = coupon
                                }
                            )
                            Text("${coupon.name} - ${(coupon.discountRate * 100).toInt()}% 할인")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("최종 결제 금액: $finalPrice 원")

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        selectedCoupon?.let { c ->
                            val foundEntry = couponMap?.entries?.find { it.value == c }
                            if (foundEntry != null) {
                                couponMap.remove(foundEntry.key)
                            }
                        }
                        vm.loggedInUser?.purchasedBooks?.add(book)
                        vm.navigate("home")
                    }
                ) {
                    Text("결제")
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
fun EditInfoScreen(
    user: UserInfo,
    onSaveUserInfo: (UserInfo) -> Unit
) {
    var name by rememberSaveable { mutableStateOf(user.name) }
    var age by rememberSaveable { mutableStateOf(user.age) }
    var gender by rememberSaveable { mutableStateOf(user.gender) }
    var email by rememberSaveable { mutableStateOf(user.email) }
    var cardType by rememberSaveable { mutableStateOf(user.cardType) }
    var cardNumber by rememberSaveable { mutableStateOf(user.cardNumber) }
    var address by rememberSaveable { mutableStateOf(user.address) }
    var phone by rememberSaveable { mutableStateOf(user.phone) }
    var birthDate by rememberSaveable { mutableStateOf(user.birthdate) }
    val vm:vm= viewModel()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
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
                label = { Text("이름") }
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = age,
                onValueChange = { newValue ->
                    if (newValue.all { it.isDigit() }) age = newValue
                },
                label = { Text("나이") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("성별: ")
                RadioButton(
                    selected = (gender == "남"),
                    onClick = { gender = "남" }
                )
                Text("남")

                Spacer(modifier = Modifier.width(8.dp))

                RadioButton(
                    selected = (gender == "여"),
                    onClick = { gender = "여" }
                )
                Text("여")
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("이메일") }
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text("결제카드:")
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = cardType == "삼성",
                    onClick = { cardType = "삼성" }
                )
                Text("삼성")

                Spacer(modifier = Modifier.width(8.dp))

                RadioButton(
                    selected = cardType == "신한",
                    onClick = { cardType = "신한" }
                )
                Text("신한")

                Spacer(modifier = Modifier.width(8.dp))

                RadioButton(
                    selected = cardType == "농협",
                    onClick = { cardType = "농협" }
                )
                Text("농협")
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = cardNumber,
                onValueChange = { newValue ->
                    if (newValue.all { it.isDigit() }) cardNumber = newValue
                },
                label = { Text("카드번호") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("주소") }
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { newValue ->
                    if (newValue.all { it.isDigit() }) phone = newValue
                },
                label = { Text("전화번호") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = birthDate,
                onValueChange = { birthDate = it },
                label = { Text("생년월일") }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
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
                )
                onSaveUserInfo(updatedUser)
                vm.navigate("manage")
            }) {
                Text("저장")
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {vm.navigate("manage")}) {
                Text("취소")
            }
        }
    }
}

@Composable
fun FavoriteScreen() {
    val vm: vm = viewModel()
    val user = vm.loggedInUser

    var isLiteratureChecked by rememberSaveable(user) {
        mutableStateOf(user?.favorits?.contains("문학") == true)
    }
    var isScienceChecked by rememberSaveable(user) {
        mutableStateOf(user?.favorits?.contains("과학") == true)
    }
    var isMathChecked by rememberSaveable(user) {
        mutableStateOf(user?.favorits?.contains("수학") == true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "관심분야 설정", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = isLiteratureChecked,
                onCheckedChange = { isLiteratureChecked = it }
            )
            Text("문학")
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = isScienceChecked,
                onCheckedChange = { isScienceChecked = it }
            )
            Text("과학")
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = isMathChecked,
                onCheckedChange = { isMathChecked = it }
            )
            Text("수학")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val selectedFavorites = mutableListOf<String>()
                if (isLiteratureChecked) selectedFavorites.add("문학")
                if (isScienceChecked) selectedFavorites.add("과학")
                if (isMathChecked) selectedFavorites.add("수학")

                user?.favorits?.clear()
                user?.favorits?.addAll(selectedFavorites)

                vm.navigate("manage")
            }
        ) {
            Text("저장")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { vm.navigate("manage") }) {
            Text("뒤로가기")
        }
    }
}


@Composable
fun CouponScreen(
) {
   val vm :vm = viewModel()
    var user = vm.loggedInUser!!

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text(text = "쿠폰함", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        if (user.coupons.isEmpty()) {
            Text(text = "보유 쿠폰이 없습니다.")
        } else {
            user.coupons.forEach { coupon ->
                Text(text = "${coupon.value.name} - 할인율: ${(coupon.value.discountRate * 100).toInt()}% (${coupon.value.description})")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {vm.navigate("manage") }) {
            Text("뒤로가기")
        }
    }
}


@Composable
fun CartScreen() {
    val vm: vm = viewModel()
    val user = vm.loggedInUser ?: return

    // 장바구니, 구매목록 상태
    var updatedCartedBooks by rememberSaveable { mutableStateOf(user.cartBooks) }
    var updatedPurchasedBooks by rememberSaveable { mutableStateOf(user.purchasedBooks) }

    // 쿠폰 선택 다이얼로그 표시여부
    var couponSelectionVisible by rememberSaveable { mutableStateOf<String?>(null) }
    // 선택된 쿠폰 객체
    var selectedCoupon by rememberSaveable { mutableStateOf<Coupon?>(null) }

    // 장바구니/구매목록 변경 -> user 객체에 반영
    LaunchedEffect(updatedCartedBooks) {
        user.cartBooks = updatedCartedBooks
    }
    LaunchedEffect(updatedPurchasedBooks) {
        user.purchasedBooks = updatedPurchasedBooks
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("장바구니:")

        if (updatedCartedBooks.isNotEmpty()) {
            updatedCartedBooks.forEach { book ->
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("- ${book.title}  - ${book.price}원")

                        Row {
                            // 장바구니에서 제거
                            Button(onClick = {
                                updatedCartedBooks = updatedCartedBooks
                                    .filterNot { it.title == book.title }
                                    .toMutableList()
                            }) {
                                Text("삭제")
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // 구매하기: 쿠폰 선택 UI 표시
                            Button(onClick = {
                                couponSelectionVisible = book.title
                                selectedCoupon = null
                            }) {
                                Text("구매하기")
                            }
                        }
                    }


                    if (couponSelectionVisible == book.title) {
                        Spacer(modifier = Modifier.height(8.dp))

                        if (user.coupons.isEmpty()) {
                            Text("보유 쿠폰이 없습니다.")
                        } else {
                            Text("사용할 쿠폰을 선택하세요:")
                            user.coupons.forEach { (key, coupon) ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = (selectedCoupon == coupon),
                                        onClick = { selectedCoupon = coupon }
                                    )
                                    Text("${coupon.name} - ${(coupon.discountRate * 100).toInt()}% 할인")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        // 결제하기 버튼
                        Button(onClick = {
                            val basePrice = book.price
                            val finalPrice = if (selectedCoupon != null) {
                                (basePrice * (1 - selectedCoupon!!.discountRate)).toInt()
                            } else basePrice

                            updatedPurchasedBooks = (updatedPurchasedBooks + book).toMutableList()
                            updatedCartedBooks = updatedCartedBooks.filterNot { it == book }.toMutableList()

                            selectedCoupon?.let { usedCoupon ->
                                val foundEntry = user.coupons.entries.find { it.value == usedCoupon }
                                if (foundEntry != null) {
                                    user.coupons.remove(foundEntry.key)
                                }
                            }

                            couponSelectionVisible = null
                            selectedCoupon = null
                        }) {
                            Text("결제하기")
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        // 취소 버튼
                        Button(onClick = {
                            couponSelectionVisible = null
                            selectedCoupon = null
                        }) {
                            Text("취소")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        } else {
            Text("장바구니가 비었습니다.")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { vm.navigate("manage") }) {
            Text("뒤로가기")
        }
    }
}


@Composable
fun PurchasedScreen(
) {
    val vm:vm= viewModel()
    var user =vm.loggedInUser!!
    var updatedPurchasedBooks by rememberSaveable { mutableStateOf(user.purchasedBooks) }
    fun getRandomDeliveryStatus(): String {
        return when (Random.nextInt(3)) {
            0 -> "배송 중"
            1 -> "배송 완료"
            else -> "배송 예정"
        }
    }
    LaunchedEffect(updatedPurchasedBooks) {
        user.purchasedBooks = updatedPurchasedBooks!!
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text("구매내역:")

        if (updatedPurchasedBooks.isNotEmpty()==true) {
            updatedPurchasedBooks.forEach { book ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${book.title}  - ${book.price}원 [${getRandomDeliveryStatus()}]")

                    Button(modifier = Modifier,
                        onClick = {
                        updatedPurchasedBooks =
                            updatedPurchasedBooks.filterNot { it.title == book.title }.toMutableList()
                    }) {
                        Text("환불")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))  // 책 항목 사이의 간격
            }
        } else {
            Text("구매내역이 없습니다.")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {vm.navigate("manage")}) {
            Text("뒤로가기")
        }
    }
}


@Composable
fun ReplyScreen() {
    val vm:vm= viewModel()
    val user = vm.loggedInUser!!

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text(
            text = "${user.name} 님이 작성한 댓글 목록",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (user.comments.isNotEmpty()) {
            LazyColumn {
                items(user.comments) { comment ->
                    comment.Books.forEach { book ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("댓글 내용: ${comment.text}")
                                Text("책 제목: ${book.title}")
                            }
                            Button(onClick = {     vm.book = book
                                vm.navigate( "detail")} ) {
                                Text("이동")
                            }
                        }
                    }
                    Divider()
                }
            }
        } else {
            Text("작성한 댓글이 없습니다.")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {vm.navigate("manage")}) {
            Text("뒤로가기")
        }
    }
}


@Composable
fun RegisterScreen(
) {
    val vm:vm= viewModel()
    var userId by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var name by rememberSaveable { mutableStateOf("") }
    var age by rememberSaveable { mutableStateOf("") }
    var gender by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var birthDate by rememberSaveable { mutableStateOf("") }
    var errorMessage by rememberSaveable { mutableStateOf("") }

    var cardType by rememberSaveable { mutableStateOf("") }
    var cardNumber by rememberSaveable { mutableStateOf("") }
    var address by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }

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
            TextField(
                value = userId,
                onValueChange = { userId = it },
                label = { Text("아이디") },
                modifier = Modifier.fillMaxWidth()
            )
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
                    selected = gender == "남",
                    onClick = { gender = "남" }
                )
                Text("남")
                RadioButton(
                    selected = gender == "여",
                    onClick = { gender = "여" }
                )
                Text("여")
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
                text =  if (birthDate.isEmpty()) "생년월일 선택" else birthDate,
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("결제카드")
                RadioButton(
                    selected = cardType == "삼성",
                    onClick = { cardType = "삼성" }
                )
                Text("삼성")
                RadioButton(
                    selected = cardType == "신한",
                    onClick = { cardType = "신한" }
                )
                Text("신한")
                RadioButton(
                    selected = cardType == "농협",
                    onClick = { cardType = "농협" }
                )
                Text("농협")
            }
        }

        item {
            TextField(
                value = cardNumber,
                onValueChange = { newValue ->
                    if (newValue.all { it.isDigit() }) {
                        cardNumber = newValue
                    }
                },
                label = { Text("카드번호를 공백없이 입력") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
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
                    if (vm.users.containsKey(userId)) {
                        errorMessage = "같은 id가 존재합니다."
                    } else if (password != confirmPassword) {
                        errorMessage = "비밀번호가 일치하지 않습니다."
                    } else if (userId.isEmpty() || password.isEmpty() || name.isEmpty() || age.isEmpty() || gender.isEmpty() || email.isEmpty() || birthDate.isEmpty()) {
                        errorMessage = "모든 항목을 입력해주세요."
                    } else {
                        errorMessage = ""
                        vm.users[userId] =  UserInfo(userId, password, name, age, gender, email,cardType,cardNumber,address,phone,birthDate)
                        vm.users[userId]?.coupons?.set(1, Coupon("30% 할인쿠폰", "가입 혜택!", 0.3))
                        vm.users[userId]?.coupons?.set(2, Coupon("50% 할인쿠폰", "최초 로그인 혜택!", 0.5))
                        vm.navigate("login")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("회원가입")
            }
        }

        item {
            Button(
                onClick = {vm.navigate("home")},
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("뒤로가기")
            }
        }
    }
}


@Composable
fun StarRatingBar(
    rating: Int,
    onRatingChanged: (Int) -> Unit
) {
    Row {
        for (starIndex in 1..5) {
            IconButton(
                onClick = {
                    onRatingChanged(starIndex)
                }
            ) {
                if (starIndex <= rating) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Star $starIndex",
                        tint = Color.Yellow
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Star,
                        contentDescription = "Star $starIndex",
                    )
                }
            }
        }
    }
}


private fun applyFinalFilterAndSort(
    filteredBySearch: List<BookInfo>,
    filteredByGenre: List<BookInfo>,
    selectedSort: String,
    selectedOrder: String,
    onResult: (List<BookInfo>) -> Unit
) {
    val intersected = filteredBySearch.intersect(filteredByGenre).toList()

    val sortedList = when (selectedSort) {
        "댓글순" -> intersected.sortedBy { it.com }
        "가나다순" -> intersected.sortedBy { it.title }
        "별점순" -> intersected.sortedBy { it.rating }
        else -> intersected
    }

    val finalResult = if (selectedOrder == "오름차순") {
        sortedList
    } else {
        sortedList.reversed()
    }

    onResult(finalResult)
}


