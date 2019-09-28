/*
 *	www.templatemo.com
 *******************************************************/

/* HTML document is loaded. DOM is ready. 
-----------------------------------------*/
$(document).ready(function(){
	
	/* Close the widget when clicked on close button */
	$('.templatemo-content-widget .fa-times').click(function(){
		$(this).parent().slideUp(function(){
			$(this).hide();
		});
	});

	
	var elements = document.getElementsByTagName('*'),
		i;
	for (i in elements) {
		if (elements[i].hasAttribute && elements[i].hasAttribute('data-include')) {
			fragment(elements[i], elements[i].getAttribute('data-include'));
		}
	}
	function fragment(el, url) {
		var localTest = /^(?:file):/,
			xmlhttp = new XMLHttpRequest(),
			status = 0;

		xmlhttp.onreadystatechange = function() {
			/* if we are on a local protocol, and we have response text, we'll assume
 *  				things were sucessful */
			if (xmlhttp.readyState == 4) {
				status = xmlhttp.status;
			}
			if (localTest.test(location.href) && xmlhttp.responseText) {
				status = 200;
			}
			if (xmlhttp.readyState == 4 && status == 200) {
				el.outerHTML = xmlhttp.responseText;
			}
		}

		try { 
			xmlhttp.open("GET", url, true);
			xmlhttp.send();
		} catch(err) {
			/* todo catch error */
		}
	}
	
	//window.location.pathname - show current page;
	$("#form-load-repository").submit(function() {
		alert("here");
                    var file1 = this[0].files[0];
                    var file2 = this[1].files[0];

                    var formData = new FormData();
                    formData.append("fake-key-1", file1);
                    formData.append("fake-key-2", file2);

                    $.ajax({
                        method:'POST',
                        data: formData,
                        url: "/load-repository",
                        processData: false, // Don't process the files
                        contentType: false, // Set content type to false as jQuery will tell the server its a query string request
                        timeout: 4000,
                        error: function(e) {
                            console.error("Failed to submit");
                            $("#result").text("Failed to get result from server " + e);
                        },
                        success: function(r) {
                            $("#result").text(r);
                        }
                    });

                    // return value of the submit operation
                    // by default - we'll always return false so it doesn't redirect the user.
                    return false;
                })
				
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
	
	var file_is_uploaded = getUrlParameter('file_upload');
	if (file_is_uploaded == "success") {
		var x = document.getElementById("div_message_success");
        x.style.display = "block";
        x.innerHTML = "XML File uploaded successfully";
	} else if (file_is_uploaded != null) {
		var x = document.getElementById("div_message_error");
        x.style.display = "block";
        x.innerHTML = "XML File uploaded failed!";
	}
});
