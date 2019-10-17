$(function() { // onload...do
    var getUrlParameter = function getUrlParameter(sParam) {
        var sPageURL = window.location.search.substring(1),
            sURLVariables = sPageURL.split('&'),
            sParameterName,
            i;

        for (i = 0; i < sURLVariables.length; i++) {
            sParameterName = sURLVariables[i].split('=');

            if (sParameterName[0] === sParam) {
                return sParameterName[1] === undefined ? true : decodeURIComponent(sParameterName[1]);
            }
        }
    };

    var user_selected = getUrlParameter('user');
    if (user_selected == null) {
        get_users();
    } else {
        get_user_repositories(user_selected);
    }
})

function usersLoaderHandler() {
    if (this.status == 200 && this.response != null) {
        get_users_details(jQuery.parseJSON(this.response));
    }
}

function get_users() {
    var usersLoader = new XMLHttpRequest();
    usersLoader.onload = usersLoaderHandler;
    usersLoader.open("GET", "users", true);
    usersLoader.setRequestHeader("Content-Type", "text/plain;charset=UTF-8");
    usersLoader.send();
};

function get_user_repositories(user_name) {
    $("#user-name-header").html(" User: " + user_name);
    $("#users-container").load("single-user.html");
};

function get_users_details(data) {
    var json = data;
    var index = 1;

    $.each(data, function(key, value) {
        $('#user-table tr:last')
            .after('<tr ' + addTagsForTR("user_" + index, "window.location='?user=" + value + "'") + '><td>' +
                index +
                '</td><td>' +
                value +
                '</td></tr>');
        index++;
    });
};