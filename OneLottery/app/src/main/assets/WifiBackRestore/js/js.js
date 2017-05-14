$(document).ready(function() {	
    $(".backupWrap").hover(function() {   	
		$('.backuphover').show();
		$('.backup').hide();
	}, function() {
		
		$('.backuphover').hide();
		$('.backup').show();
	}); 
	$(".restoreWrap").hover(function() {   	
		$('.restorehover').show();
		$('.restore').hide();
	}, function() {
		
		$('.restorehover').hide();
		$('.restore').show();
	});
	$(".backupWrap").click(function() {   	
		$('.tck').show();
	});  
	$(".ann").click(function() {   	
		$('.tck').hide();
	});
	$(".ann2").click(function() {   	
		$('.tck').hide();
	});
	$(".restoreWrap").click(function() {   	
		$('.tck1').show();
	});  
	$(".ann").click(function() {   	
		$('.tck1').hide();
	});
	$(".ann2").click(function() {   	
		$('.tck1').hide();
	});
})

