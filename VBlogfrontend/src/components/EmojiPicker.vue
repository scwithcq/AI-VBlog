<script setup>
import { ref } from 'vue'
import { ElPopover, ElButton } from 'element-plus'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:modelValue'])

// 常用表情集合
const emojis = [
  // 笑脸
  '😀', '😃', '😄', '😁', '😆', '😅', '🤣', '😂',
  '🙂', '🙃', '😉', '😊', '😇', '🥰', '😍', '🤩',
  '😘', '😗', '😚', '😙', '🥲', '😋', '😛', '😜',
  '🤪', '😝', '🤑', '🤗', '🤭', '🤫', '🤔', '🤐',

  // 表示
  '🤨', '😐', '😑', '😶', '😏', '😒', '🙄', '😬',
  '🤥', '😌', '😔', '😪', '🤤', '😷', '🤒', '🤕',
  '🤢', '🤮', '🤧', '🤨', '🤬', '😵', '🤯', '🤠',

  // 爱你
  '😳', '�', '😦', '😧', '😨', '😰', '😥', '😢',
  '😭', '😱', '😖', '😣', '😞', '😓', '😩', '😫',

  // 手势
  '👋', '🤚', '🖐️', '✋', '🖖', '👌', '🤌', '🤏',
  '✌️', '🤞', '🫰', '🤟', '🤘', '🤙', '👍', '👎',
  '☝️', '👆', '👇', '👈', '👉', '👏', '🙌', '👐',

  // 爱心
  '❤️', '🧡', '💛', '💚', '💙', '💜', '🖤', '🤍',
  '🤎', '💔', '💕', '💞', '💓', '💗', '💖', '💘',

  // 其他常用
  '⭐', '✨', '⚡', '🔥', '💯', '🎉', '🎊', '👏',
  '🙏', '💪', '👊', '✊', '👊', '🤜', '🤛', '💥'
]

const showPicker = ref(false)

const selectEmoji = (emoji) => {
  emit('update:modelValue', props.modelValue + emoji)
  showPicker.value = false
}

const clearEmoji = () => {
  emit('update:modelValue', '')
}
</script>

<template>
  <div class="emoji-picker">
    <el-popover placement="top" :width="300" trigger="click" :visible="showPicker"
      @update:visible="(val) => showPicker = val">
      <template #reference>
        <el-button type="text" size="small" icon="😊">表情</el-button>
      </template>

      <div class="emoji-grid">
        <button v-for="(emoji, index) in emojis" :key="index" class="emoji-btn" @click="selectEmoji(emoji)">
          {{ emoji }}
        </button>
      </div>
      <div class="emoji-actions">
        <el-button size="small" text @click="clearEmoji">清空</el-button>
      </div>
    </el-popover>
  </div>
</template>

<style scoped>
.emoji-picker {
  display: inline-block;
}

.emoji-grid {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 4px;
  padding: 10px;
  max-height: 300px;
  overflow-y: auto;
}

.emoji-btn {
  width: 100%;
  height: 32px;
  font-size: 20px;
  border: none;
  background: none;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.2s;

  &:hover {
    background-color: #f0f0f0;
    transform: scale(1.2);
  }

  &:active {
    transform: scale(0.9);
  }
}

.emoji-actions {
  text-align: center;
  padding: 8px 0;
  border-top: 1px solid #ebeef5;
}
</style>
