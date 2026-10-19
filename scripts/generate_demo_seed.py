"""Generate idempotent SQL. Only reserved demo_* accounts are touched; no real-user notifications.
Run: python scripts/generate_demo_seed.py > private/community-demo.sql
Use a UTF-8 capable shell, or --output private/community-demo.sql.
"""
from pathlib import Path
import argparse

PEOPLE = [
    ('demo_linqiu', '林秋的散步地图', '下班后走路回家，周末寻找小店。', '杭州'),
    ('demo_zhouye', '周野的代码本', '应用开发学习中，喜欢把踩坑记录写清楚。', '成都'),
    ('demo_momo', '茉茉今天吃什么', '做饭、咖啡和普通日子里的小惊喜。', '广州'),
    ('demo_xiaoyu', '小屿慢慢拍', '用镜头记录光，也记录没那么完美的瞬间。', '厦门'),
    ('demo_achen', '阿晨跑两圈', '慢跑选手，不拼配速，先坚持出门。', '武汉'),
    ('demo_yueban', '月半读书中', '阅读、电影，以及一些零碎的想法。', '南京'),
]
POSTS = [
    (0, '城市散步', '今天提前一站下车，居然发现一条开满小花的巷子。原来回家的路也可以每天不一样。'),
    (1, '开发日记', '终于把消息的未读状态理顺了：打开会话标记已读，右上角数量也要一起更新。看起来小小的功能，做起来全是细节。'),
    (2, '好好吃饭', '工作日快手晚饭：番茄鸡蛋面，加一小把青菜。洗锅只要一分钟，这点对我很重要。'),
    (3, '随手记录', '整理相册才发现，上周拍得最多的居然是树影。下午四点的光真的不用加滤镜。'),
    (4, '运动打卡', '今天只跑了三公里，但出门前纠结了半小时。完成比完美更能让我继续。'),
    (5, '阅读碎片', '读书时最喜欢的一刻，是某句话刚好解释了最近说不清楚的心情。大家最近在看什么？'),
    (0, '周末计划', '想把周末留半天给自己：不赶行程，带本书去河边坐坐。如果下雨就改成窗边。'),
    (1, '开发日记', '给项目补测试之后，才知道“我点过了没问题”和“边界情况也没问题”差多少。今天先补不同账号之间的数据隔离。'),
    (2, '咖啡时间', '手冲又有点酸了，准备下次把研磨调细一点点。厨房实验每天都能有新变量。'),
    (3, '城市散步', '等红灯时看到对面楼里一排暖灯。很多时候所谓城市氛围感，就是有人正在过普通的生活。'),
    (4, '运动打卡', '降温后的第一跑，开头有点冷，两公里以后刚刚好。跑完喝热豆浆，今天的快乐很具体。'),
    (5, '周末计划', '和朋友约好各自带一本喜欢的书交换，不做读书报告，只说最喜欢的一个片段。感觉比买新书更期待。'),
    (0, '好好吃饭', '菜市场买到一袋小橘子，老板多塞了两个。回家路上剥了一个，很甜，连今天的加班都没那么烦了。'),
    (1, '开发日记', '今天学到：前端按钮隐藏只是交互，真正的权限一定要在服务端再检查一次。给未来的自己留个提醒。'),
    (2, '周末计划', '试着做一次冰箱清空料理。剩米饭、半根胡萝卜、两个鸡蛋，最后比认真照菜谱做的还香。'),
    (3, '随手记录', '最近练习每次出门只拍三张。少按几次快门，反而会更认真看眼前的东西。'),
    (4, '城市散步', '今天不跑步，陪朋友慢慢走了一圈。运动手表没有漂亮的数据，但聊天聊得很开心。'),
    (5, '阅读碎片', '给自己新定的规则：每天读十页也算完成，不再因为没有整块时间就把书一直放着。')
]
REPLIES = [
    ('我也试过换一条路回家，经常有惊喜。', '下次准备再往前走一个路口看看。'),
    ('这个细节很重要，之前总分不清哪里还有未读。', '对，还要确认另一个账号不会串状态。'),
    ('加一点菌菇也很好吃，而且不费时间。', '记下了，明天就试一下！'),
    ('树影很适合慢慢看，风吹的时候尤其好看。', '是的，照片反而拍不出当时那种感觉。'),
    ('能迈出门就已经赢一半了。', '明天继续，先不立太大的目标。'),
    ('我最近在看散文，睡前读几页很放松。', '这个节奏我喜欢，准备也试一试。'),
]

def quote(value):
    # SQL uses hex literals: no SQL mode or shell quoting ambiguity.
    return "CONVERT(0x" + value.encode('utf-8').hex() + " USING utf8mb4)"

def generate():
    out = ['SET NAMES utf8mb4;', 'START TRANSACTION;']
    for i, (username, name, bio, city) in enumerate(PEOPLE):
        q = quote(username)
        out += [f"INSERT INTO tb_user(username,password,nickname,avatar,bio,location,create_time) SELECT {q},'!demo-login-disabled',{quote(name)},'/avatars/demo-{i}.svg',{quote('【虚构演示账号】'+bio)},{quote(city)},DATE_SUB(NOW(),INTERVAL {21+i} DAY) WHERE NOT EXISTS (SELECT 1 FROM tb_user WHERE username={q});",
                f"SET @u{i}=(SELECT user_id FROM tb_user WHERE username={q} AND bio LIKE {quote('【虚构演示账号】%')});"]
    for i, (author, topic, text) in enumerate(POSTS):
        content = quote(f'#{topic}# {text}')
        hours = 72 - i*4
        out += [f'INSERT INTO tb_post(user_id,content,create_time) SELECT @u{author},{content},DATE_SUB(NOW(),INTERVAL {hours} HOUR) WHERE NOT EXISTS (SELECT 1 FROM tb_post WHERE user_id=@u{author} AND content={content});',
                f'SET @p{i}=(SELECT MIN(post_id) FROM tb_post WHERE user_id=@u{author} AND content={content});']
        for liker in range(6):
            if liker != author and (liker+i)%3 != 0:
                out.append(f'INSERT IGNORE INTO tb_post_like(post_id,user_id,create_time) VALUES(@p{i},@u{liker},DATE_ADD((SELECT create_time FROM tb_post WHERE post_id=@p{i}),INTERVAL {10+liker*7} MINUTE));')
        if i < 12:
            comment, reply = REPLIES[i%6]
            visitor=(author+1)%6
            for parent, commenter, body, minutes in [('NULL',visitor,comment,30),('@c',author,reply,44)]:
                body=quote(body)
                out.append(f'INSERT INTO tb_comment(post_id,user_id,parent_id,content,create_time) SELECT @p{i},@u{commenter},{parent},{body},DATE_ADD((SELECT create_time FROM tb_post WHERE post_id=@p{i}),INTERVAL {minutes} MINUTE) WHERE NOT EXISTS (SELECT 1 FROM tb_comment WHERE post_id=@p{i} AND user_id=@u{commenter} AND content={body});')
                if parent=='NULL':
                    out.append(f'SET @c=(SELECT MIN(comment_id) FROM tb_comment WHERE post_id=@p{i} AND user_id=@u{commenter} AND content={body});')
    for i in range(6):
        j=(i+1)%6
        for a,b in [(i,j),(j,i)]:
            out.append(f'INSERT IGNORE INTO tb_follow(follower_id,followee_id) VALUES(@u{a},@u{b});')
            out.append(f'INSERT INTO tb_friendship(user_id,friend_id,status) SELECT @u{a},@u{b},1 WHERE NOT EXISTS (SELECT 1 FROM tb_friendship WHERE user_id=@u{a} AND friend_id=@u{b});')
    for topic in sorted({p[1] for p in POSTS}):
        out.append(f'INSERT IGNORE INTO tb_topic(name,heat) VALUES({quote(topic)},3);')
    out += ['COMMIT;', "SELECT 'demo_users',COUNT(*) FROM tb_user WHERE username LIKE 'demo\\_%';", "SELECT 'demo_posts',COUNT(*) FROM tb_post WHERE user_id IN (SELECT user_id FROM tb_user WHERE username LIKE 'demo\\_%');"]
    return '\n'.join(out)+'\n'

if __name__ == '__main__':
    parser=argparse.ArgumentParser(); parser.add_argument('--output'); args=parser.parse_args()
    if args.output: Path(args.output).write_text(generate(),encoding='utf-8',newline='\n')
    else: print(generate())
