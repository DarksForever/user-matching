package com.example.usercenter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.usercenter.bean.Tag;
import com.example.usercenter.service.TagService;
import com.example.usercenter.mapper.TagMapper;
import org.springframework.stereotype.Service;

/**
* @author ASUS
* @description 针对表【tag(标签)】的数据库操作Service实现
* @createDate 2023-05-13 23:25:33
*/
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
    implements TagService{

}




