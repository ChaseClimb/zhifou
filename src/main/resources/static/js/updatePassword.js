function updatePassword() {

    //获取文本框的值
    var password = $("#password").val();
    var newPassword = $("#newPassword").val();
    //发送请求
    $.post("/user/update", {"password": password, "newPassword": newPassword}, function (data) {
        if (data.code == 999) {
            window.location.href = '/login';
        } else if (data.code == 1) {
            alert(data.msg);
        } else {
            alert("修改密码成功！");
            location.reload();
        }
    }, "json");
}




