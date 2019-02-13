function addQuestion() {
    //获取文本框的值
    var title = $("#title").val();
    var content = $("#content").val();
    //发送请求
    $.post("/question/add", {"title": title, "content": content}, function (data) {
        if (data.code == 999) {
            window.location.href = '/login?next=' + window.encodeURIComponent(window.location.href);
        } else if (data.code == 1) {
            alert("发布问题失败");
        } else {
            location.reload();
        }
    });
}