package com.example.coffeeshop.Utils

object ValidationUtils {
    
    /**
     * Regex pattern cho số điện thoại Việt Nam:
     * - Bắt đầu bằng: 03, 05, 07, 08, 09
     * - Tổng cộng 10 chữ số
     */
    private val PHONE_PATTERN = Regex("^(03|05|07|08|09)[0-9]{8}$")
    
    /**
     * Regex pattern cho email:
     * - Chỉ chấp nhận @gmail.com và @ut.edu.vn
     */
    private val EMAIL_PATTERN_GMAIL = Regex(
        "^[a-zA-Z0-9._%+-]+@gmail\\.com$"
    )
    
    private val EMAIL_PATTERN_UT = Regex(
        "^[a-zA-Z0-9._%+-]+@ut\\.edu\\.vn$"
    )
    
    /**
     * Regex pattern cho tên:
     * - Ký tự đầu tiên phải viết hoa
     * - Ít nhất 2 từ (ví dụ: "Nguyễn Anh")
     * - Mỗi từ bắt đầu bằng chữ hoa
     * - Cho phép dấu tiếng Việt
     */
    private val NAME_PATTERN = Regex(
        "^[A-ZÀÁẠẢÃÂẦẤẬẨẪĂẰẮẶẲẴÈÉẸẺẼÊỀẾỆỂỄÌÍỊỈĨÒÓỌỎÕÔỒỐỘỔỖƠỜỚỢỞỠÙÚỤỦŨƯỪỨỰỬỮỲÝỴỶỸĐ][a-zàáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđ]+(\\s+[A-ZÀÁẠẢÃÂẦẤẬẨẪĂẰẮẶẲẴÈÉẸẺẼÊỀẾỆỂỄÌÍỊỈĨÒÓỌỎÕÔỒỐỘỔỖƠỜỚỢỞỠÙÚỤỦŨƯỪỨỰỬỮỲÝỴỶỸĐ][a-zàáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđ]+)+$"
    )
    
    /**
     * Mật khẩu: tối thiểu 8 ký tự, tối đa 16 ký tự
     */
    private val PASSWORD_MIN_LENGTH = 8
    private val PASSWORD_MAX_LENGTH = 16
    
    /**
     * Validate số điện thoại Việt Nam
     * @param phone Số điện thoại cần validate
     * @return Pair<Boolean, String> - (isValid, errorMessage)
     */
    fun validatePhone(phone: String): Pair<Boolean, String> {
        if (phone.isEmpty()) {
            return Pair(false, "Vui lòng nhập số điện thoại")
        }
        
        if (!phone.matches(PHONE_PATTERN)) {
            return Pair(false, "Số điện thoại phải có 10 chữ số và bắt đầu bằng 03, 05, 07, 08 hoặc 09")
        }
        
        return Pair(true, "")
    }
    
    /**
     * Validate email - chỉ chấp nhận @gmail.com và @ut.edu.vn
     * @param email Email cần validate
     * @return Pair<Boolean, String> - (isValid, errorMessage)
     */
    fun validateEmail(email: String): Pair<Boolean, String> {
        if (email.isEmpty()) {
            return Pair(false, "Vui lòng nhập email")
        }
        
        // Kiểm tra email có đúng 1 ký tự @
        val parts = email.split("@")
        if (parts.size != 2) {
            return Pair(false, "Email không hợp lệ. Phải có đúng 1 ký tự @")
        }
        
        val username = parts[0]
        val domain = parts[1].lowercase()
        
        // Kiểm tra username không được rỗng
        if (username.isEmpty()) {
            return Pair(false, "Email không hợp lệ. Phần trước @ không được để trống")
        }
        
        // Kiểm tra chỉ chấp nhận @gmail.com hoặc @ut.edu.vn
        val isValidGmail = email.matches(EMAIL_PATTERN_GMAIL)
        val isValidUT = email.matches(EMAIL_PATTERN_UT)
        
        if (!isValidGmail && !isValidUT) {
            return Pair(false, "Email chỉ chấp nhận đuôi @gmail.com hoặc @ut.edu.vn")
        }
        
        return Pair(true, "")
    }
    
    /**
     * Validate họ và tên đệm (có thể có nhiều từ)
     * @param lastName Họ và tên đệm
     * @return Pair<Boolean, String> - (isValid, errorMessage)
     */
    fun validateLastName(lastName: String): Pair<Boolean, String> {
        if (lastName.isEmpty()) {
            return Pair(false, "Vui lòng nhập họ & tên đệm")
        }
        
        // Kiểm tra mỗi từ bắt đầu bằng chữ hoa
        val words = lastName.trim().split("\\s+".toRegex())
        for (word in words) {
            if (word.isEmpty()) continue
            // Kiểm tra ký tự đầu tiên phải là chữ cái và phải viết hoa
            val firstChar = word[0]
            if (!firstChar.isLetter() || !firstChar.isUpperCase()) {
                return Pair(false, "Mỗi từ trong họ & tên đệm phải bắt đầu bằng chữ hoa (ví dụ: Nguyễn Ngọc, không chấp nhận: Nguyễn ngọc)")
            }
        }
        
        return Pair(true, "")
    }
    
    /**
     * Validate tên (chỉ 1 từ, bắt đầu bằng chữ hoa)
     * @param firstName Tên
     * @return Pair<Boolean, String> - (isValid, errorMessage)
     */
    fun validateFirstName(firstName: String): Pair<Boolean, String> {
        if (firstName.isEmpty()) {
            return Pair(false, "Vui lòng nhập tên")
        }
        
        val trimmed = firstName.trim()
        
        // Kiểm tra không có khoảng trắng (chỉ 1 từ)
        if (trimmed.contains(" ")) {
            return Pair(false, "Tên chỉ được nhập 1 từ (ví dụ: Anh, Nam, Linh)")
        }
        
        // Kiểm tra bắt đầu bằng chữ hoa
        if (!trimmed[0].isUpperCase() || !trimmed[0].isLetter()) {
            return Pair(false, "Tên phải bắt đầu bằng chữ hoa (ví dụ: Anh, Nam)")
        }
        
        return Pair(true, "")
    }
    
    /**
     * Validate mật khẩu
     * @param password Mật khẩu cần validate
     * @return Pair<Boolean, String> - (isValid, errorMessage)
     */
    fun validatePassword(password: String): Pair<Boolean, String> {
        if (password.isEmpty()) {
            return Pair(false, "Vui lòng nhập mật khẩu")
        }
        
        if (password.length < PASSWORD_MIN_LENGTH) {
            return Pair(false, "Mật khẩu phải có ít nhất $PASSWORD_MIN_LENGTH ký tự")
        }
        
        if (password.length > PASSWORD_MAX_LENGTH) {
            return Pair(false, "Mật khẩu không được vượt quá $PASSWORD_MAX_LENGTH ký tự")
        }
        
        return Pair(true, "")
    }
    
    /**
     * Validate tên (họ và tên)
     * @param name Tên cần validate
     * @return Pair<Boolean, String> - (isValid, errorMessage)
     */
    fun validateName(name: String): Pair<Boolean, String> {
        if (name.isEmpty()) {
            return Pair(false, "Vui lòng nhập họ tên")
        }
        
        // Kiểm tra ít nhất 2 từ
        val words = name.trim().split("\\s+".toRegex())
        if (words.size < 2) {
            return Pair(false, "Họ tên phải có ít nhất 2 từ (ví dụ: Nguyễn Anh)")
        }
        
        // Kiểm tra mỗi từ bắt đầu bằng chữ hoa
        if (!name.matches(NAME_PATTERN)) {
            return Pair(false, "Mỗi từ phải bắt đầu bằng chữ hoa (ví dụ: Nguyễn Anh)")
        }
        
        return Pair(true, "")
    }
    
    /**
     * Validate xác nhận mật khẩu
     * @param password Mật khẩu
     * @param confirmPassword Mật khẩu xác nhận
     * @return Pair<Boolean, String> - (isValid, errorMessage)
     */
    fun validateConfirmPassword(password: String, confirmPassword: String): Pair<Boolean, String> {
        if (confirmPassword.isEmpty()) {
            return Pair(false, "Vui lòng xác nhận mật khẩu")
        }
        
        if (password != confirmPassword) {
            return Pair(false, "Mật khẩu xác nhận không khớp")
        }
        
        return Pair(true, "")
    }
}

