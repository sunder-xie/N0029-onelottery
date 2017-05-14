'use strict'
const wrapper = require('co-mysql');
const pool = wrapper(global.pool);
var config = require('../../config');
const util = require('../../lib/util');
const streamBuffers = require('stream-buffers');
const parse = require('co-busboy');
var fs= require('fs');
var path = require('path');



exports.get = function*() {
    var sql = 'select * from Banner order by imgOrder';
    var rules = yield pool.query(sql);
    yield this.render('activity/banner', {
        data:rules
    })
}
exports.post = function*() {

   // let parts = parse(this);
    let  part,imgName;
    let stream = new streamBuffers.WritableStreamBuffer()
    var parts = parse(this, {
        // only allow upload `.jpg` files
        checkFile: function (fieldname, file, filename) {
            console.log(filename)
            if (path.extname(filename) !== '.jpg') {
                var err = new Error('invalid jpg image')
                err.status = 400
                return err
            }
        }
    })
    var imgdata = {};
    while (part = yield parts) {
        if (part != null)
            if (!(part instanceof Array)) {
                part.pipe(stream);
                if(path.extname(part.filename)== '.jpg'){
                    imgName=part.filename;
                   // console.log(part.filename)
                }
            } else {
                imgdata[part[0]] = part[1]
                //console.log("password value is " ,part[0],part[1]);
            }
    }
    console.log(1,imgdata)
    console.log(2,imgName)

    var regex = /(https?:\/\/)?(\w+\.?)+(\/[a-zA-Z0-9\?%=_\-\+\/]+)?/gi;
    imgdata.imgaddress = imgdata.imgaddress.replace(regex, function(match, capture) {
        if (capture) {
            return match
        } else {
            return "http://"+match;
        }
    });
    
    imgName = new Date().getTime()+imgName;
    let data = stream.getContents();
    try {
        fs.writeFileSync(path.join(__dirname, '../../public/banner/'+imgName), data);
        var sql = "insert into Banner set ?";
        yield pool.query(sql, {
            imgName: imgName,
            imgAddress: imgdata.imgaddress,
            imgOrder: parseInt(imgdata.order)

        })
        this.response.body = '添加成功';
    } catch (e) {
        console.log(e);
        this.response.body = '添加失败';
    }
}
