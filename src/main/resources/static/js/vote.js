/*
* 点赞1 点踩-1 不点踩不点赞0
* likedObj.val() 隐藏域更新初始状态
* */


//点赞
function follow(dom, entityType, entityId) {
    var likedObj = $(dom).parent().prev(".liked");
    var liked = likedObj.val();

    //未点赞 未点踩
    var followFlag = 0;
    var unfollowFlag = 0;

    if (liked == 1) {
        followFlag = 1;
    } else if (liked == -1) {
        unfollowFlag = 1;
    }

    if (followFlag == 1 && unfollowFlag == 0) {
        cancelFollow(dom, entityType, entityId, likedObj);
    } else {
        $.post("/like", {"entityType": entityType, "entityId": entityId}, function (data) {
            if (data.code == 999) {
                window.location.href = '/login?next=' + window.encodeURIComponent(window.location.href);
            } else if (data.code == 1) {
                alert(data.msg);
            } else {
                //回调函数
                if (followFlag == 0) {
                    if (unfollowFlag == 0) {
                        $(dom).addClass("active");
                    } else if (unfollowFlag == 1) {
                        $(dom).next().removeClass("active");
                        $(dom).addClass("active");
                    }
                    likedObj.val(1);
                    //更新点赞数
                    $(dom).children(".likeNum").text(data.msg);
                }
            }
        }, "json");
    }
}

//点踩
function unfollow(dom, entityType, entityId) {
    var likedObj = $(dom).parent().prev(".liked");
    var liked = likedObj.val();

    //未点赞 未点踩
    var followFlag = 0;
    var unfollowFlag = 0;

    if (liked == 1) {
        followFlag = 1;
    } else if (liked == -1) {
        unfollowFlag = 1;
    }

    if (unfollowFlag == 1 && followFlag == 0) {
        cancelUnfollow(dom, entityType, entityId,likedObj)
    }
    else{
        $.post("/dislike", {"entityType": entityType, "entityId": entityId}, function (data) {
            if (data.code == 999) {
                window.location.href = '/login?next=' + window.encodeURIComponent(window.location.href);
            } else if (data.code == 1) {
                alert(data.msg);
            } else {
                //回调函数
                if (unfollowFlag == 0) {
                    if (followFlag == 0) {
                        $(dom).addClass("active");
                    } else if (followFlag == 1) {
                        $(dom).prev().removeClass("active");
                        $(dom).addClass("active");
                    }
                    likedObj.val(-1);
                    $(dom).prev().children(".likeNum").text(data.msg);
                }

            }
        }, "json");
    }
}


//取消点赞
function cancelFollow(dom, entityType, entityId, likedObj) {
    //alert("取消点赞");
    $.post("/cancelFollow", {"entityType": entityType, "entityId": entityId}, function (data) {
        if (data.code == 999) {
            window.location.href = '/login?next=' + window.encodeURIComponent(window.location.href);
        } else if (data.code == 1) {
            alert(data.msg);
        } else {
            $(dom).removeClass("active");
            likedObj.val(0);
            $(dom).children(".likeNum").text(data.msg);
        }
    }, "json");
}


//取消点踩
function cancelUnfollow(dom, entityType, entityId, likedObj) {
    //alert("取消点踩")
    $.post("/cancelUnfollow", {"entityType": entityType, "entityId": entityId}, function (data) {
        if (data.code == 999) {
            window.location.href = '/login?next=' + window.encodeURIComponent(window.location.href);
        } else if (data.code == 1) {
            alert(data.msg);
        } else {
            $(dom).removeClass("active");
            likedObj.val(0);
            //$(dom).prev().children(".likeNum").text(data.msg);
        }
    }, "json");
}