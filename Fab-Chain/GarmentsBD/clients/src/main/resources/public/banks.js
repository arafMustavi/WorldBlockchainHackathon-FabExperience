$(document).ready(function(){
    
    var apiBaseURL = "http://" + window.location.host + "/api/example/";
    

    //-------------------
    //
    //         CREATE TOKEN
    //
    //-------------------

    // $("#create_token_button").click(function(){
    //     var token_name = $("#create_token_name").val();
    //     var token_val = $("#valuation_amount").val();
    //     console.log("Create Token Started");
    //     console.log("Create Token Started2");
    //     CREATE_TOKEN_PATH = apiBaseURL + "create-token";

    //     $.ajax({
    //         url: CREATE_TOKEN_PATH,
    //         type: 'post',
    //         data: JSON.stringify({
    //             currencyName : token_name.toString(),
    //             valuation: token_val.toString()
    //                             }),
    //         headers: {
    //             "Content-type": "application/json; charset=UTF-8"
    //         },
    //         dataType: 'json',
    //         success: function (data) {
    //             console.info(data);
    //         }
    //     });
    // });


    //-------------------
    //
    //         ISSUE TOKEN
    //
    //-------------------
    $("#issue_token_button").click(function(){
        var bank_name = $("#issue_bank_name").val();
        var amount = $("#issue_amount").val();
        console.log("Issue Token Started");
        ISSUE_TOKEN_PATH = apiBaseURL + "issue-token";
        $.ajax({
            url: ISSUE_TOKEN_PATH,
            type: 'post',
            data: JSON.stringify({
                                    receiver : bank_name.toString(),
                                    currency : "taka",
                                    quantity : amount.toString()
                            }),
            headers: {
                "Content-type": "application/json; charset=UTF-8"
            },
            dataType: 'json',
            success: function (data) {
                console.info(data);
                alert( amount + " Token Issued For " + bank_name );
            //    Text Box Div e Message Show kore dibo
            }
        }
        );
    });

    //-------------------
    //
    //         MOVE TOKEN
    //
    //-------------------


    $('#move_token_button').click(function(){
        var bank_name = $("#move_receiver_name").val();
        var amount = $("#move_amount").val();
        console.log("Move Token Started");

        MOVE_TOKEN_PATH = apiBaseURL + "move-token";
        $.ajax({
            url: MOVE_TOKEN_PATH,
            type: 'post',
            data: JSON.stringify({
                receiver : bank_name.toString(),
                currency : "taka",
                quantity : amount.toString()
            }),
            headers: {

                "Content-type": "application/json; charset=UTF-8"
            },
            dataType: 'json',
            success: function (data) {
                console.info(data);
                alert( amount + " Token Moved to " + bank_name );                

            }
        });
    });

    //-------------------
    //
    //         In Vault TOKEN / get-token
    //
    //-------------------
    $("#get_token_button").click(function(){
        console.log("Get Token Started");
        GET_TOKEN_PATH = apiBaseURL + "get-token";
        function handleResponse(data) {
            alert(data);
        }
        $.ajax({
                url: GET_TOKEN_PATH,
                type: 'post',
                data: JSON.stringify({
                    // receiver : bank_name.toString(),
                    currency : "taka",
                    // quantity : amount.toString()
                }),
                headers: {
                    "Content-type": "application/json; charset=UTF-8"
                },
                dataType: 'json',
                success: function (response) {
                    console.info(response);
                    alert(response.message);
                }
            }
        );
    });

});



