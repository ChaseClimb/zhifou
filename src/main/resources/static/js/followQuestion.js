function followQuestion(questionId) {
    //发送请求
    $.post("/followQuestion", {"questionId": questionId}, function (data) {
        if (data.code == 999) {
            window.location.href = '/login?next=' + window.encodeURIComponent(window.location.href);
        } else if (data.code == 1) {
            alert(data.msg);
        } else {
            location.reload();
        }
    }, "json");
}

function unfollowQuestion(questionId) {
    //发送请求
    $.post("/unfollowQuestion", {"questionId": questionId}, function (data) {
        if (data.code == 999) {
            window.location.href = '/login?next=' + window.encodeURIComponent(window.location.href);
        } else if (data.code == 1) {
            alert(data.msg);
        } else {
            location.reload();
        }
    }, "json");
}