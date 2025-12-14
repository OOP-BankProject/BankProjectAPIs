Database Configuration 
spring.datasource.url=jdbc:postgresql://localhost:5432/bankdb spring.datasource.username=your_username
spring.datasource.password=your_password spring.datasource.driver-class-name=org.postgresql.Driver 
JWT Configuration 
jwt.secret=YourSuperSecretKeyForJWTTokenGenerationMinimum256Bits jwt.expiration.session=300000
jwt.expiration.verification=600000 jwt.expiration.access=3600000 jwt.expiration.refresh=604800000 
Server Configuration 
server.port=8080 
REGISTER ENDPOINTS 
post: /api/register/step1 
Request 
{ “fin”: “ABC1234”, “phoneNumber”: “+994501234567” } 
Response 
{ “success”: true, “message”: “OTP kodu telefon nömrənizə göndərildi”, “data”: { “message”: “OTP kodu telefon nömrənizə göndərildi”,
“sessionToken”: “eyJhbGciOiJIUzI1NiJ9...”, “otpExpirySeconds”: 300 }, “timestamp”: “2024-12-14T10:30:00” } 
POST /api/register/resend-otp 
Request 
{ “sessionToken”: “eyJhbGciOiJIUzI1NiJ9...” } 
post: /api/register/verify-otp 
Request 
{ “sessionToken”: “eyJhbGciOiJIUzI1NiJ9...”, “otpCode”: “123456” } 
Response 
{ “success”: true, “message”: “OTP kodu təsdiqləndi”, “data”: { “message”: “OTP kodu təsdiqləndi. İndi qeydiyyatı tamamlaya bilərsiniz”,
“verified”: true, “verificationToken”: “eyJhbGciOiJIUzI1NiJ9...” }, “timestamp”: “2024-12-14T10:31:00” } 
post: /api/register/step2 
Request 
{ “verificationToken”: “eyJhbGciOiJIUzI1NiJ9...”, “firstName”: “John”, “lastName”: “Doe”, “dateOfBirth”: “1995-05-15”, “email”:
“john.doe@example.com”, “password”: “SecurePass123”, “confirmPassword”: “SecurePass123” } 
Response{ “success”: true, “message”: “Qeydiyyat uğurla tamamlandı”, “data”: { “message”: “Qeydiyyat uğurla tamamlandı”, “userId”: 1, “email”:
“john.doe@example.com”, “phoneNumber”: “+994501234567” }, “timestamp”: “2024-12-14T10:35:00” } 
LOGIN ENDPOINTS 
post: /api/login 
Request 
{ “fin”: “ABC1234”, “password”: “SecurePass123” } 
Response 
{ “success”: true, “message”: “Uğurla daxil oldunuz”, “data”: { “message”: “Uğurla daxil oldunuz”, “accessToken”: “eyJhbGciOiJIUzI1NiJ9...”,
“refreshToken”: “eyJhbGciOiJIUzI1NiJ9...”, “userId”: 1, “email”: “john.doe@example.com”, “phoneNumber”: “+994501234567”, “firstName”:
“John”, “lastName”: “Doe” }, “timestamp”: “2024-12-14T11:00:00” } 
POST /api/refresh 
Request 
{ “refreshToken”: “eyJhbGciOiJIUzI1NiJ9...” } 
Response 
{ “success”: true, “message”: “Token yeniləndi”, “data”: { “accessToken”: “eyJhbGciOiJIUzI1NiJ9...”, “refreshToken”: “eyJhbGciOiJIUzI1NiJ9...”
}, “timestamp”: “2024-12-14T12:00:00” } 
PASSWORD RESET ENDPOINTS 
POST /api/password/forgot/step1 
Request 
{ “fin”: “ABC1234”, “phoneNumber”: “+994501234567” } 
Response 
{ “success”: true, “message”: “OTP kodu göndərildi”, “data”: { “message”: “Parol sıfırlama kodu telefon nömrənizə göndərildi”,
“resetToken”: “eyJhbGciOiJIUzI1NiJ9...”, “otpExpirySeconds”: 300 }, “timestamp”: “2024-12-14T13:00:00” } 
POST /api/password/forgot/verify-otp 
Request 
{ “resetToken”: “eyJhbGciOiJIUzI1NiJ9...”, “otpCode”: “123456” } 
Response 
{ “success”: true, “message”: “OTP kodu təsdiqləndi”, “data”: { “message”: “OTP kodu təsdiqləndi. İndi yeni parol təyin edə bilərsiniz”,
“verified”: true, “passwordResetToken”: “eyJhbGciOiJIUzI1NiJ9...” }, “timestamp”: “2024-12-14T13:05:00” } 
POST /api/password/resetRequest 
{ “passwordResetToken”: “eyJhbGciOiJIUzI1NiJ9...”, “newPassword”: “NewSecurePass123”, “confirmPassword”: “NewSecurePass123” } 
Response 
{ “success”: true, “message”: “Parol uğurla yeniləndi”, “data”: { “message”: “Parolunuz uğurla yeniləndi”, “fin”: “ABC1234” }, “timestamp”:
“2024-12-14T13:10:00” } 
POST /api/password/change 
Request 
{ “fin”: “ABC1234”, “oldPassword”: “OldPass123”, “newPassword”: “NewSecurePass123”, “confirmPassword”: “NewSecurePass123” } 
Response 
{ “success”: true, “message”: “Parol dəyişdirildi”, “data”: { “message”: “Parolunuz uğurla dəyişdirildi” }, “timestamp”:
“2024-12-14T14:00:00”
