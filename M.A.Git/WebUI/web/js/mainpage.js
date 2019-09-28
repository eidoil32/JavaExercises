$(function() { // onload...do
    $.ajax({
        data: {x: "username"},
        url: "mainPage",
        timeout: 2000,
        error: function() {
            console.error("Error from server!");
        },
        success: function(data) {
            if (data == "0001") {
                location.href = "login.html";
            } else {
                document.getElementById("username").innerHTML = data;
            }
        }
    });

})