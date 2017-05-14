$(function(){
	$('.Activity .title li').click(function(event) {
		/* Act on the event */
		$(this).addClass('current').siblings() .removeClass('current');
		
		$('.Activity .del ul').eq($(this).index()).show().siblings().hide();
	});
	$('.act_regular .title li').click(function(event) {
		var ifmsrc = $(this).attr("ifmsrc");
		/* Act on the event */
		$(this).addClass('current').siblings().removeClass('current');

		$('.act_regular .fancy iframe').attr("src",ifmsrc);


		//$('.act_regular .fancy').eq($(this).index()).show().siblings().hide();
	});
	$(".site_word").hover(function() {
		$('.site_dropUp').show();
	}, function() {
		$('.site_dropUp').hide();
	}); 
		$(".site_peo").hover(function() {
		$('.site_dropUp').show();
	}, function() {
		$('.site_dropUp').hide();
	});




	$(".per-acctit-btn-second").click(function() {
		$('.per-acctit-btn2-gray').hide();
		$('.per-acctit-btn1-gray').show();	
		$('.per-acc-bot').hide();
		$('.per-acc-form').show();
		$(".submitinput").hide();
	}); 
	$(".per-acctit-btn-first").click(function() {
		$('.per-acctit-btn2-gray').show();
		$('.per-acctit-btn1-gray').hide();
		$('.per-acc-bot').show();
		$('.per-acc-form').hide();
		$(".submitinput").hide();
	}); 



	$(".act-fin-col4").hover(function() {
		$(this).children('.bet-pop').show();
	}, function() {
		$(this).children('.bet-pop').hide();
	}); 


	$(".type-check").click(function() {
		$('.per-acctit-btn2-gray').show();
	}); 


	
	$(".creat-pop-pub-btn .fase-btn").click(function() {
		$('.creat-reg-pop').hide();
	}); 


	$(".creat-li").dblclick(function() {
		$('.creat-reg-pop').show();
	}); 

	$('.shehzh').click(function(event) {
		$('.Shzhong').show();
		$(this).css({'background':"#eee"});
	});
	$('.shz-con span').click(function(event) {
		$('.Shzhong').hide();
		$('.shehzh').css({'background':"#fff"});
	});

	$('.dakuan').click(function(event) {
		$('.Yida').show();
		$(this).css({'background':"#eee"});

	});
	$('.shz-con span').click(function(event) {
		$('.Yida').hide();
		$('.dakuan').css({'background':"#fff"});
	});

	$('.queren').click(function(event) {
		$('.Yiqueren').show();
		$(this).css({'background':"#eee"});
	});
	$('.shz-con span').click(function(event) {
		$('.Yiqueren').hide();
		$('.queren').css({'background':"#fff"});
	});

	$('.tiqu').click(function(event) {
		$('.Tixian').show();
		$(this).css({'background':"#eee"});

	});
	$('.shz-con span').click(function(event) {
		$('.Tixian').hide();
		$('.tiqu').css({'background':"#fff"});

	});

	$('.shensu').click(function(event) {
		$('.keShensu').show();
		$(this).css({'background':"#eee"});
	});
	$('.shz-con span').click(function(event) {
		$('.keShensu').hide();
		$('.shensu').css({'background':"#fff"});
	});
	
	$('.aler-yy button').click(function(event) {
		$('.aler-yy').hide();
		$('.shehzh').css({'background':"#fff"});
	});
	$('.Dayu').click(function(event) {
		$('.aler-yy').show();
		$('.keShensu').hide();
		$('.Shzhong').hide();
	});
	$('.Quxiao').click(function(event) {
		$('.aler-yy').hide();
		$('.order-input').hide();
		$('.keShensu').hide();
		$('.Shzhong').hide();
	});

	$('.queren-tra-btt').click(function(event) {
		$('.order-input').show();
		$('.keShensu').hide();
		$('.Shzhong').hide();
	});
	
	$('.alert-con button').click(function(event) {
		$('.alertWrap').hide();
		$('.shehzh').css({'background':"#fff"});
		$('.shensu').css({'background':"#fff"});
	});

/*
	$('.creat-pop-pub-btn .succ-btn').click(function(){
		var length = $('.creat-li').length;
		var Linshi = $('.creat-li:last').clone(true);
		$('.creat-ul').append(Linshi);
		$('.creat-reg-pop').hide();
		
	});*/
});

function init(){
	if("\v"=="v"){//IE浏览器
		document.getElementById("monthContainer").onpropertychange=changeValue;
	}else{ // 其他浏览器
		document.getElementById("monthContainer").addEventListener("input",changeValue(),false);
	}
}
function init1(){
	if("\v"=="v"){//IE浏览器
		document.getElementById("dateContainer").onpropertychange=changedata;
	}else{ // 其他浏览器
		document.getElementById("dateContainer").addEventListener("input",changedata(),false);
	}
}