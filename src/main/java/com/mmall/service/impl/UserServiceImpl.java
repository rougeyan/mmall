package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ServiceResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("iUserService")
public class UserServiceImpl implements IUserService {
    @Autowired
    private UserMapper userMapper;
    // 登录
//    @Override
    public ServiceResponse<User> login(String username, String password) {
        int resultCount = userMapper.checkUserName(username);
        if(resultCount ==0){
            return ServiceResponse.createByErrorMessage("用户名不存在");
        }

        // todo 密码登录md5
        // 这里比较是加密过得MD5密码
        String md5password = MD5Util.MD5EncodeUtf8(password);
        User user = userMapper.selectLogin(username,md5password);
        if(user == null){
            return ServiceResponse.createByErrorMessage("账号或者密码错误");
        }
        // 设置为空
        user.setPassword(StringUtils.EMPTY);

        return ServiceResponse.createBySuccess("登录成功",user);
    }

    /**
     * 注册
     * @param user
     * @return
     */
    public ServiceResponse<String> register(User user) {
        // 因为有可能注册传的是username / email 复用到this.checkValid
        // 首先校验username 用户名是否存在;
        ServiceResponse validResponse = this.checkValid(user.getUsername(),Const.USERNAME);
        if(!validResponse.isSuccess()){
            return  validResponse;
        }
        // 校验email是否存在;
        validResponse = this.checkValid(user.getEmail(),Const.EMAIL);
        if(!validResponse.isSuccess()){
            return  validResponse;
        }

        // 默认设置为普通用户;
        user.setRole(Const.Role.Role_CUSTOMER);
        // 不能设置密码为明文; MD5加密
        int resultCount = userMapper.insert(user);
        if(resultCount == 0){
            return ServiceResponse.createByErrorMessage("注册失败");
        }
        return ServiceResponse.createBySuccessMessage("注册成功");
    }

    /**
     * 失焦校验(用户名 / email)
     * @param str  数据
     * @param type 数据类型
     * @return
     */
    public ServiceResponse<String> checkValid(String str,String type){
        // isNoneBlank 空是返回 false
        if(StringUtils.isNoneBlank(type)){
            if(Const.USERNAME.equals(type)){
                int resultCount = userMapper.checkUserName(str);
                if(resultCount > 0){
                    return ServiceResponse.createByErrorMessage("用户名已经存在");
                }
            }
            if(Const.EMAIL.equals(type)){
                int resultCount = userMapper.checkEmail(str);
                if(resultCount > 0){
                    return ServiceResponse.createByErrorMessage("email已经存在");
                }
            }
        }else{
            return ServiceResponse.createByErrorMessage("参数错误");
        }
        return ServiceResponse.createBySuccessMessage("校验成功");
    }

    /**
     * 忘记密码
     * @param username
     * @return
     */
    public ServiceResponse selectQuestion(String username){
        ServiceResponse validResponse = this.checkValid(username,Const.USERNAME);
        if(validResponse.isSuccess()){
            // 用户不存在
            return  ServiceResponse.createByErrorMessage("用户不存在");
        }
        String question = userMapper.selectQuestionByUsername(username);
        // 成功情况;
        if(StringUtils.isNoneBlank(question)){
            return ServiceResponse.createBySuccess(question);
        }
        return ServiceResponse.createByErrorMessage("找回密码的问题是空的");
    }

    /**
     * 检测答案是否正确
     * @param username
     * @param question
     * @param answer
     * @return
     */
    public ServiceResponse<String> checkAnswer(String username,String question, String answer){
        int resultCount = userMapper.checkAnswer(username, question, answer);
        if(resultCount > 0){
            String forgetToken = UUID.randomUUID().toString();
            // 把本地token 放在本地cache中, 设置有效期;
            TokenCache.setKey(TokenCache.TOKEN_PREFIX+username,forgetToken);
            return  ServiceResponse.createBySuccess(forgetToken);

        }
        return ServiceResponse.createByErrorMessage("问题的答案错误");
    }

    /**
     * 忘记密码 => 重置密码
     * @param username
     * @param passwordnew
     * @param forgetToken
     * @return
     */
    public ServiceResponse<String> forgetResetPassword(String username, String passwordnew,String forgetToken){
        // 校验token;
        if(StringUtils.isBlank(forgetToken)){
            return ServiceResponse.createByErrorMessage("参数错误,token需要传递");
        }
        // 校验用户
        ServiceResponse validResponse = this.checkValid(username,Const.USERNAME);
        if(validResponse.isSuccess()){
            // 用户不存在
            return  ServiceResponse.createByErrorMessage("用户不存在");
        }
        // 获取token
        String token = TokenCache.getKey(TokenCache.TOKEN_PREFIX+username);
        // 判定token;
        if(StringUtils.isBlank(token)){
            System.out.println("[token]:"+token);
            return  ServiceResponse.createByErrorMessage("token无效或过期");
        }
        // equal判断 通过token校验
        if(StringUtils.equals(forgetToken,token)){
            String md5password = MD5Util.MD5EncodeUtf8(passwordnew);
            int rowCount = userMapper.updatePasswordByUsername(username,md5password);
            if (rowCount > 0){
                return ServiceResponse.createBySuccessMessage("修改密码成功");
            }
        }else{
            return ServiceResponse.createByErrorMessage("token错误,请重新获取重置密码的token");
        }
        return ServiceResponse.createByErrorMessage("修改密码失败");
    }

    /**
     *  登录状态下重置密码
     * @param passwordOld
     * @param passwordNew
     * @param user
     * @return
     */
    public ServiceResponse<String> resetPassword(String passwordOld,String passwordNew, User user){
        // 防止横向越权,要校验这个用户的旧密码,一定要指向是这个用户, 因为我们会查询一个count(1)
        // 如果不指定id 那么结果就是true啦count>0
        int resultCount = userMapper.checkPassword(MD5Util.MD5EncodeUtf8(passwordOld),user.getId());
        if(resultCount == 0){
            return ServiceResponse.createByErrorMessage("旧密码错误");
        }
        user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
        int updateCount = userMapper.updateByPrimaryKeySelective(user);
        if(updateCount > 0){
            return ServiceResponse.createBySuccessMessage("密码更新成功");
        }
        return ServiceResponse.createByErrorMessage("密码更新失败");
    }

    public ServiceResponse<User> updateInformation(User user){
        // username 不能被更新
        // email 也是进行一个校验, 校验新的email是否已经存在,
        // 并且email如果相同的话, 不能是我们当前的这个用户;
        int resultCount = userMapper.checkEmailByUserId(user.getEmail(),user.getId());
        if(resultCount>0){
            return ServiceResponse.createByErrorMessage("email已经存在,请更换email");
        }
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());

        int updateCount = userMapper.updateByPrimaryKeySelective(updateUser);

        if(updateCount>0){
            return ServiceResponse.createBySuccess("更新个人信息成功",updateUser);
        }
        return ServiceResponse.createByErrorMessage("更新个人信息失败");
    }


    public ServiceResponse<User> getInformation(Integer userId){
        User user = userMapper.selectByPrimaryKey(userId);
        if(user == null){
            return ServiceResponse.createByErrorMessage("找不到当前用户");
        }
        user.setPassword(StringUtils.EMPTY);
        return ServiceResponse.createBySuccess(user);

    }


    // backend
    // 检测是否是管理员
    public ServiceResponse checkAdminRole(User user){
       if(user !=null && user.getRole().intValue() == Const.Role.Role_ADMIN){
           return ServiceResponse.createBySuccess();
       }
       return ServiceResponse.createByError();
    }

}
