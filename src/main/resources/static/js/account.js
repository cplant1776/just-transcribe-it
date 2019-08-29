$("#changePasswordBtn").click(function() {
    $("#pwField").replaceWith($(".edit-pw-popout"));
    $("#changePasswordBtn").attr("hidden", "true");
    console.log("hit");
});

function bindViewButton(transcriptId) {
    elmId = "transcript-btn-" + transcriptId;
    console.log("Bound: " + elmId);
    $("#" + elmId).click(function() {
        window.location.href="/transcribe/view/" + transcriptId;
        console.log("checked");
    });
};

$( document ).ready(function() {
    for (var i=0; i < transcripts.length; i++)
    {
            bindViewButton(transcripts[i].id);
    }
});