function followUser(userId) {
    //发送请求
    $.post("/followUser", {"userId": userId}, function (data) {
        if (data.code == 999) {
            window.location.href = '/login?next=' + window.encodeURIComponent(window.location.href);
        } else if (data.code == 1) {
            alert("有问题");
            alert(data.msg);
        } else {
            location.reload();
        }
    }, "json");
}


function unfollowUser(userId) {
    //发送请求
    $.post("/unfollowUser", {"userId": userId}, function (data) {
        if (data.code == 999) {
            window.location.href = '/login?next=' + window.encodeURIComponent(window.location.href);
        } else if (data.code == 1) {
            alert("有问题");
            alert(data.msg);
        } else {
            location.reload();
        }
    }, "json");
}
