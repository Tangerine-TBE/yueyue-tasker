
module.exports = function (__runtime__, scope){

    var business = {};
    business.create = function(){
        return new THKLBusiness();
    }
    return business;
    function THKLBusiness(){
        this._business = new com.stardust.autojs.core.business.JsDataProvider();
        this.report = function(json){
             this._business.requestFeedBackFromJs(json);
        }
    }
}