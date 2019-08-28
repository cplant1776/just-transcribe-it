$("#changePasswordBtn").click(function() {
    $("#pwField").replaceWith($(".edit-pw-popout"));
    $("#changePasswordBtn").attr("hidden", "true");
    console.log("hit");
});