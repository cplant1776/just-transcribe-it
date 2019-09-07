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
        error: function(e){
            console.log("ERROR: " + e)
        },
        beforeSend: function() {
            "sending..."
        }
    });
}; 

function sendDownloadReq () {
    console.log("Downloading...")
    download(userGivenName + ".txt", transcriptText);
}; 

function download(filename, text) {
    var element = document.createElement('a');
    element.setAttribute('href', 'data:text/plain;charset=utf-8,' + encodeURIComponent(text));
    element.setAttribute('download', filename);
  
    element.style.display = 'none';
    document.body.appendChild(element);
  
    element.click();
  
    document.body.removeChild(element);
  }


$(document).ready(function () {
    // Disable form action
    $('#deleteForm').submit(function (event) {
        event.preventDefault();
    });

});