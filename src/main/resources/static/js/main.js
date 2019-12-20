
function delete_job(id) {
    console.log("Sending DELETE for " + id);
    $.ajax({
        url: '/nlpaas/job/' + id,
        type: 'DELETE'
    }).done(function() {
            console.log("Done callback.")
            $('#delete-button').hide();
            $('#delete-message').text("Job " + id + " has been deleted. The output file may still be available for download.")
            $('#delete-message-div').show();
    }).fail(function(xhr,status,error) {
            console.log("Fail callback.")
            console.log(xhr.status + ": " + xhr.statusText)
            // console.log(JSON.parse(status));
            // console.log(JSON.parse(error))
            $('#delete-button').hide();
            $('#delete-message').text('Unable to remove job with id ' + id)
            $('#delete-message-div').show();
    });

}

function test_ajax(id) {
    console.log("testing ajax")
    $.ajax({
        url:'/test/ajax/'+id,
        type: 'GET',
        success: function(result) {
            console.log("Success")
            $('#message').text('Success')
            $('#message').show()
        },
        error: function(xhr, status, error) {
            console.log("There was an error.")
            $('#message').text('Error')
            $('#message').show()
        }
    });
}

function test(n) {
    console.log("Running test with " + n);
    $('#info').toggle()
    $('#message').toggle()
}