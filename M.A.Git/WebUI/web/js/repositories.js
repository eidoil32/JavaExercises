$(function() {
    $.ajax({
        data: {},
        url: "repositories",
        timeout: 2000,
        error: function() {
            console.error("Error from server!");
        },
        success: function(data) {
            var receivedData = [];

            $.each(data.jsonArray, function(index) {
                $.each(data.jsonArray[index], function(key, value) {
                    var point = [];
                    point.push(key);
                    point.push(value);
                    receivedData.push(point);
                    }); 
            });
            console.log(receivedData);
        }
    });
	
	$("#pull-request").text("Pull request (1)");
});