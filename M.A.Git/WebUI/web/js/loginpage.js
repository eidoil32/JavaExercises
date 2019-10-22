$(function() { // onload...do
    $("#loginForm").submit(function() {
        let parameters = $(this).serialize();

        $.ajax({
            data: parameters,
            url: this.action,
			async: true, 
            timeout: 2000,
            error: function() {
                console.error("Error from server!");
            },
            success: function(data) {
              if (data === "success") {
                location.href = "index.html";
              } else {
                let x = document.getElementById("errorblock");
                x.style.display = "block";
                x.innerHTML = data;
              }
            }
        });

        return false;
    })
});