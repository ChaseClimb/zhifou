
function addMessage() {
    //获取文本框的值
    var toId = $("#toId").val();
    var content = $("#messageContent").val();
    //发送请求
    $.post("/msg/add", {"toId": toId, "content": content}, function (data) {
        if (data.code == 999) {
            window.location.href = '/login?next=' + window.encodeURIComponent(window.location.href);
        } else if (data.code == 1) {
            alert(data.msg);
        } else {
            location.reload();
            alert("发送成功");
        }
    },"json");
}

function replyMessage(){

    var toId = $("#targetUserId").val();
    var content = $("#replyMessage").val();
    $.post("/msg/add", {"toId": toId, "content": content}, function (data) {
        if (data.code == 999) {
            window.location.href = '/login?next=' + window.encodeURIComponent(window.location.href);
        } else if (data.code == 1) {
            alert(data.msg);
        } else {
            location.reload();
            alert("回复成功");
        }
    },"json");
}