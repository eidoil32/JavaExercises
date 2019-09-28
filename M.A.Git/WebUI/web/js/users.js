$(function() { // onload...do
    $.ajax({
        data: {},
        url: "users",
        timeout: 2000,
        error: function() {
            console.error("Error from server!");
        },
        success: function(data) {
            
        }
    });
})