package com.mandarin.bcu.androidutil

import android.content.Context
import android.util.SparseArray
import androidx.core.util.isNotEmpty
import com.mandarin.bcu.androidutil.io.ErrorLogWriter
import common.pack.PackData
import common.util.lang.MultiLangCont
import common.util.stage.MapColc
import common.util.stage.Music
import common.util.stage.SCDef
import common.util.stage.Stage
import common.util.unit.Enemy
import java.util.*
import kotlin.collections.ArrayList

object FilterStage {
    fun setFilter(name: String, stmname: String, enemies: List<Int>, enemorand: Boolean, music: String, bg: Int, star: Int, bh: Int, bhop: Int, contin: Int, boss: Int, c: Context) : Map<String, SparseArray<ArrayList<Int>>> {
        val result = HashMap<String, SparseArray<ArrayList<Int>>>()

        val mc = MapColc.values()?.toMutableList() ?: return result

        for(n in 0 until StaticStore.mapcode.size) {
            val i = StaticStore.mapcode[n]
            val m = mc[0]

            val stresult = SparseArray<ArrayList<Int>>()

            for(j in m.maps.indices) {
                val stm = m.maps[j] ?: continue

                val sresult = ArrayList<Int>()

                for(k in 0 until stm.list.size) {
                    val s = stm.list[k] ?: continue

                    val nam = if(stmname != "") {
                        if(name != "") {
                            val stmnam = (MultiLangCont.get(stm) ?: stm.name ?: "").toLowerCase(Locale.ROOT).contains(stmname.toLowerCase(Locale.ROOT))
                            val stnam = (MultiLangCont.get(s) ?: s.name ?: "").toLowerCase(Locale.ROOT).contains(name.toLowerCase(Locale.ROOT))

                            stmnam && stnam
                        } else {
                            (MultiLangCont.get(stm) ?: stm.name ?: "").toLowerCase(Locale.ROOT).contains(stmname.toLowerCase(Locale.ROOT))
                        }
                    } else {
                        (MultiLangCont.get(s) ?: s.name ?: "").toLowerCase(Locale.ROOT).contains(name.toLowerCase(Locale.ROOT))
                    }

                    val enem = containEnemy(enemies, s.data.allEnemy, enemorand)

                    val m0 = s.mus0.pack + " - " + s.mus0.id
                    val m1 = s.mus1.pack + " - " + s.mus1.id

                    val mus = s.mus0.get() == music.get() || s.mus1.get() == music.get() || music

                    val backg = s.bg == bg || bg == -1

                    val stars = stm.stars.size > star

                    val baseh = if(bh != -1) {
                        when(bhop) {
                            -1 -> true
                            0 -> s.health < bh
                            1 -> s.health == bh
                            2 -> s.health > bh
                            else -> false
                        }
                    } else {
                        true
                    }

                    val cont = when(contin) {
                        -1 -> true
                        0 -> !s.non_con
                        1 -> s.non_con
                        else -> false
                    }

                    val bos = when(boss) {
                        -1 -> true
                        0 -> hasBoss(s, c)
                        1 -> !hasBoss(s, c)
                        else -> false
                    }

                    if(nam && enem && mus && backg && stars && baseh && cont && bos)
                        sresult.add(k)
                }

                if(sresult.isNotEmpty())
                    stresult.put(j,sresult)
            }

            if(stresult.isNotEmpty())
                result.put(i,stresult)
        }

        return result
    }

    private fun containEnemy(src: List<Int>, target: Set<Enemy>, orand: Boolean) : Boolean {
        if(src.isEmpty()) return true

        if(target.isEmpty()) return false

        val targetid = ArrayList<Int>()

        for(ten in target) {
            if (!targetid.contains(ten.id)) {
                targetid.add(ten.id)
            }
        }

        //True for Or search

        if(orand) {
            for(i in src) {
                if(targetid.contains(i))
                    return true
            }

            return false
        } else {
            return targetid.containsAll(src)
        }
    }

    private fun hasBoss(st: Stage, c: Context) : Boolean {
        try {
            val def = st.data ?: return false

            for (i in def.datas) {
                if (i[SCDef.B] == 1)
                    return true
            }
        } catch(e: Exception) {
            ErrorLogWriter.writeLog(e, StaticStore.upload, c)
            return false
        }

        return false
    }
}