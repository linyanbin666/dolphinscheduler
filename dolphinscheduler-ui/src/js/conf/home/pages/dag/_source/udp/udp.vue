/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
<template>
  <div class="udp-model">
    <div class="scrollbar contpi-boxt">
      <div>
        <el-input
                type="text"
                size="small"
                v-model="name"
                id="inputName"
                :disabled="router.history.current.name === 'projects-instance-details'"
                :placeholder="$t('Please enter name (required)')">
        </el-input>
      </div>

      <template v-if="router.history.current.name !== 'projects-instance-details'">
        <div style="padding-top: 12px;">
          <el-input
                  type="textarea"
                  size="small"
                  v-model="description"
                  :autosize="{minRows:2}"
                  :placeholder="$t('Please enter description(optional)')"
                  autocomplete="off">
          </el-input>
        </div>
      </template>

      <div class="title" style="padding-top: 6px;">
        <span class="text-b">{{$t('select tenant')}}</span>
        <form-tenant v-model="tenantCode"></form-tenant>
      </div>
      <div class="title" style="padding-top: 6px;">
        <span class="text-b">{{$t('warning of timeout')}}</span>
        <span style="padding-left: 6px;">
          <el-switch v-model="checkedTimeout" size="small"></el-switch>
        </span>
      </div>
      <div class="content" style="padding-bottom: 10px;" v-if="checkedTimeout">
        <span>
          <el-input v-model="timeout" style="width: 160px;" maxlength="9" size="small">
            <span slot="append">{{$t('Minute')}}</span>
          </el-input>
        </span>
      </div>
      <div class="title" style="padding-top: 6px;">
        <span class="text-b">{{$t('Process execute type')}}</span>
        <span >
          <el-select
            :disabled="isDetails"
            v-model="executionType"
            size="small"
            style="width: 180px">
            <el-option
                    v-for="item in itemsList"
                    :key="item.key"
                    :value="item.key"
                    :label="$t(item.val)">
            </el-option>
          </el-select>
        </span>
      </div>
      <div class="title" style="padding-top: 6px;">
        <span>{{$t('Set global')}}</span>
      </div>
      <div class="content">
        <div>
          <m-local-params
                  ref="refLocalParams"
                  @on-local-params="_onLocalParams"
                  :udp-list="udpList"
                  :hide="false">
          </m-local-params>
        </div>
      </div>
    </div>
    <div class="bottom">
      <div class="submit">
        <template v-if="router.history.current.name === 'projects-definition-details'">
          <div class="lint-pt">
            <el-checkbox :disabled="isDetails" v-model="releaseState" size="small" :false-label="'OFFLINE'" :true-label="'ONLINE'">{{$t('Release process definition')}}</el-checkbox>
            <el-checkbox v-if="releaseState === 'ONLINE'" :disabled="isDetails"
                         v-model="releaseSchedule" size="small"
                         :false-label="false" :true-label="true">{{$t('Release schedule')}}</el-checkbox>
          </div>
        </template>
        <template v-if="router.history.current.name === 'projects-instance-details'">
          <div class="lint-pt">
            <el-checkbox :disabled="isDetails" v-model="syncDefine" size="small">{{$t('Whether to update the process definition')}}</el-checkbox>
          </div>
        </template>
        <el-button type="text" size="small" @click="close()"> {{$t('Cancel')}} </el-button>
        <el-button type="primary" size="small" round :disabled="isDetails" @click="ok()" id="btnSubmit">{{$t('Add')}}</el-button>
      </div>
    </div>
  </div>
</template>
<script>
  import _ from 'lodash'
  import i18n from '@/module/i18n'
  import mLocalParams from '../formModel/tasks/_source/localParams'
  import disabledState from '@/module/mixin/disabledState'
  import Affirm from '../jumpAffirm'
  import FormTenant from './_source/selectTenant'

  export default {
    name: 'udp',
    data () {
      return {
        originalName: '',
        // dag name
        name: '',
        // dag description
        description: '',
        // Global custom parameters
        udpList: [],
        // Global custom parameters
        udpListCache: [],
        // Whether to go online the process definition
        releaseState: 'ONLINE',
        // Release schedule or not
        releaseSchedule: true,
        // Whether to update the process definition
        syncDefine: true,
        // Timeout alarm
        timeout: 0,
        // tenant code
        tenantCode: 'default',
        // checked Timeout alarm
        checkedTimeout: true,
        // process execute type
        executionType: 'PARALLEL',
        itemsList: [
          { key: 'PARALLEL', val: 'parallel' },
          { key: 'SERIAL_WAIT', val: 'Serial wait' },
          { key: 'SERIAL_DISCARD', val: 'Serial discard' },
          { key: 'SERIAL_PRIORITY', val: 'Serial priority' }
        ]
      }
    },
    mixins: [disabledState],
    props: {
    },
    methods: {
      /**
       * udp data
       */
      _onLocalParams (a) {
        this.udpList = a
      },
      _verifTimeout () {
        const reg = /^[1-9]\d*$/
        if (!reg.test(this.timeout)) {
          this.$message.warning(`${i18n.$t('Please enter a positive integer greater than 0')}`)
          return false
        }
        return true
      },
      _accuStore () {
        const udp = _.cloneDeep(this.udpList)
        udp.forEach(u => {
          delete u.ifFixed
        })
        this.store.commit('dag/setGlobalParams', udp)

        this.store.commit('dag/setName', _.cloneDeep(this.name))
        this.store.commit('dag/setTimeout', _.cloneDeep(this.timeout))
        this.store.commit('dag/setTenantCode', _.cloneDeep(this.tenantCode))
        this.store.commit('dag/setExecutionType', _.cloneDeep(this.executionType))
        this.store.commit('dag/setDesc', _.cloneDeep(this.description))
        this.store.commit('dag/setSyncDefine', this.syncDefine)
        this.store.commit('dag/setReleaseState', this.releaseState)
        this.store.commit('dag/setReleaseSchedule', this.releaseSchedule)
      },
      /**
       * submit
       */
      ok () {
        if (!this.name) {
          this.$message.warning(`${i18n.$t('DAG graph name cannot be empty')}`)
          return
        }

        let _verif = () => {
          // verification udf
          if (!this.$refs.refLocalParams._verifProp()) {
            return
          }
          // verification timeout
          if (this.checkedTimeout && !this._verifTimeout()) {
            return
          }

          // Storage global globalParams
          this._accuStore()

          Affirm.setIsPop(false)
          this.$emit('onUdp')
        }

        if (this.originalName !== this.name) {
          this.store.dispatch('dag/verifDAGName', this.name).then(res => {
            _verif()
          }).catch(e => {
            this.$message.error(e.msg || '')
          })
        } else {
          _verif()
        }
      },
      /**
       * Close the popup
       */
      close () {
        this.$emit('close')
      },
      /**
       * reload localParam
       */
      reloadParam () {
        const dag = _.cloneDeep(this.store.state.dag)
        let fixedParam = []
        const tasks = this.store.state.dag.tasks
        for (const task of tasks) {
          const localParam = task.params ? task.params.localParams : []
          localParam.forEach(l => {
            if (!fixedParam.some(f => { return f.prop === l.prop })) {
              fixedParam.push(Object.assign({
                ifFixed: true
              }, l))
            }
          })
        }

        let globalParams = _.cloneDeep(dag.globalParams)

        globalParams = globalParams.map(g => {
          if (fixedParam.some(f => { return g.prop === f.prop })) {
            fixedParam = fixedParam.filter(f => { return g.prop !== f.prop })
            return Object.assign(g, {
              ifFixed: true
            })
          } else {
            return g
          }
        })
        let udpList = [...fixedParam, ...globalParams].sort(s => {
          if (s.ifFixed) {
            return -1
          } else {
            return 1
          }
        })
        this.udpList = udpList
        this.udpListCache = udpList
      }
    },
    watch: {
      releaseState (val) {
        this.releaseSchedule = val !== 'OFFLINE'
      },
      checkedTimeout (val) {
        if (!val) {
          this.timeout = 0
          this.store.commit('dag/setTimeout', _.cloneDeep(this.timeout))
        }
      }
    },
    created () {
      const dag = _.cloneDeep(this.store.state.dag)

      this.name = dag.name
      this.originalName = dag.name
      this.description = dag.description
      this.syncDefine = dag.syncDefine
      this.releaseState = dag.releaseState
      this.releaseSchedule = dag.releaseSchedule
      this.timeout = dag.timeout || 0
      this.checkedTimeout = this.timeout !== 0
      this.$nextTick(() => {
        if (dag.tenantCode) {
          this.tenantCode = dag.tenantCode
        } else {
          this.tenantCode = this.store.state.user.userInfo.tenantCode || 'default'
        }
      })
      this.executionType = dag.executionType
    },
    mounted () {},
    components: { FormTenant, mLocalParams }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .udp-model {
    width: 624px;
    min-height: 420px;
    background: #fff;
    border-radius: 3px;
    padding:20px 0 ;
    position: relative;
    .contpi-boxt {
      max-height: 600px;
      overflow-y: scroll;
      padding:0 20px;

      ::selection {
        background: #409EFF ;
        color: white;
      }
    }
    .title {
      line-height: 36px;
      padding-bottom: 10px;
      span {
        font-size: 16px;
        color: #333;
      }
    }
    .bottom{
      position: absolute;
      bottom: 0;
      left: 0;
      width: 100%;
      text-align: right;
      height: 56px;
      line-height: 56px;
      border-top: 1px solid #DCDEDC;
      background: #fff;
      .submit {
        padding-right: 20px;
        margin-top: -4px;
      }
      .lint-pt {
        position: absolute;
        left: 20px;
        top: -2px;
        >label {
          font-weight: normal;
        }
      }
    }
    .content {
      padding-bottom: 50px;
      .user-def-params-model {
        .add {
          a {
            color: #0097e0;
          }
        }
      }
    }

  }
</style>
