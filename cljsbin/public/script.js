function send() {
    var data = {source: $('#source').val()};
    $.ajax({
        url: '/create',
        method: 'POST',
        data: JSON.stringify(data),
        contentType: 'application/json',
        success: function(resp) {
            var src = '/html/' + resp.id;
            $('#result').attr('src', src);
        }
    });
}

$(function() {
    $('#send').on('click', send);
});
