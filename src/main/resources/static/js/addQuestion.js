var followFlag =0;
var unfollowFlag =0;
function addQuestion() {
    //获取文本框的值
    var title = $("#title").val();
    var content = $("#content").val();
    //发送请求
    $.post("/question/add", {"title": title, "content": content}, function (data) {
        if (data.code == 999) {
            window.location.href = '/login?next=' + window.encodeURIComponent(window.location.href);
        } else if (data.code == 1) {
            alert(data.msg);
        } else {
            location.reload();
        }
    },"json");
}

function follow(dom){
    if(followFlag==0){
        if(unfollowFlag==0){
            $(dom).addClass("active");
        }
        else if (unfollowFlag==1){
            $(dom).next().removeClass("active");
            $(dom).addClass("active");
            unfollowFlag=0;
        }
        followFlag=1;
    }
    //取消点赞
    else if (followFlag==1&&unfollowFlag==0){
        $(dom).removeClass("active");
        followFlag=0;
    }
}

function unfollow(dom){
    if(unfollowFlag==0){
        if(followFlag==0){
            $(dom).addClass("active");
        }
        else if (followFlag==1){
            $(dom).prev().removeClass("active");
            $(dom).addClass("active");
            followFlag=0;
        }
        unfollowFlag=1;
    }
    //取消点踩
    else if (unfollowFlag==1&&followFlag==0){
        $(dom).removeClass("active");
        unfollowFlag=0;
    }
}