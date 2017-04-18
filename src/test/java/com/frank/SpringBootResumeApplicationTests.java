package com.frank;

import com.frank.controller.BlogController;
import com.frank.dao.ArticleMapper;
import com.frank.dao.DocumentMapper;
import com.frank.dao.TagMapper;
import com.frank.dto.ArticleInfo;
import com.frank.model.Article;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringBootResumeApplicationTests {

	@Resource
	private BlogController blogController;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ArticleMapper articleMapper;

    @Resource
    private DocumentMapper documentMapper;

    @Resource
    private TagMapper tagMapper;

    @Test
    public void setString(){
        stringRedisTemplate.opsForValue().set("hello" , "world! Redis");

        String value = stringRedisTemplate.opsForValue().get("hello");
        Assert.assertEquals("world! Redis" , value);
        System.out.println("键值对："+value);
    }

    @Test
    public void setList(){
        List<String> list = new ArrayList<>(5);
        for(int i = 0 ; i < 5 ; i++) {
            String val = "uid" + i;
            list.add(val);
        }
        stringRedisTemplate.opsForList().rightPushAll("list" , list);
        //取第一个value
        String listvalue = stringRedisTemplate.opsForList().index("list" , 0);
        Assert.assertEquals("uid0" , listvalue);

        //取最后一个value
        String lastValue = stringRedisTemplate.opsForList().index("list" , 4);
        Assert.assertEquals("uid4" , lastValue);


    }

    @Test
    public void setSet(){
        stringRedisTemplate.opsForSet().add("setKey" , "aaa");
        String setValue = stringRedisTemplate.opsForSet().pop("setKey");
        Assert.assertEquals("aaa" , setValue);
    }


	@Test
	public void contextLoads() {
	    System.out.println(blogController.getArticleCount());
	}


	@Test
    public void testPageHelper(){
        PageHelper.startPage(1, 2);
        List<Article> articleList = articleMapper.selectDocuments();
//        PageInfo<Article> pageInfo = new PageInfo<>(articleList);
//        System.out.println("分页："+pageInfo);
        for (Article article : articleList){
            List<String> tags = tagMapper.selectTagsByArticle(article.getId());
            ArticleInfo articleInfo = new ArticleInfo(article,tags,false);
            System.out.println(articleInfo);
        }

    }
}
