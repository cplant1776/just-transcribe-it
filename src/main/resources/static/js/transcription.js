// Send DELETE request if delete button pressed
function sendDeleteReq () {
    $('html,body, button').css('cursor', 'wait');
    console.log("send request...")
    $.ajax({
        type: "DELETE",
        url: "/storage/deleteFile?" + $.param({"transcriptId": transcriptId}),
        dataType: "html",
        success: function(msg){
            console.log("DELETE WAS SUCCESSFUL");
            window.location.href = "/account";
        },
        error: function(msg){
            console.log("ERROR: " + msg)
        },
        beforeSend: function() {
            "sending..."
        }
    });
}; 

$(document).ready(function () {
    // Disable form action
    $('#deleteForm').submit(function (event) {
        event.preventDefault();
    });

});